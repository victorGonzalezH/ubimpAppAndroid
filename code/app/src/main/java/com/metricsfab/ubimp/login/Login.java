package com.metricsfab.ubimp.login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.metricsfab.ubimp.main.MainActivity;
import com.metricsfab.ubimp.receivers.SMSReceiveListener;
import com.metricsfab.ubimp.receivers.SMSReceiver;
import com.metricsfab.ubimp.shared.ProgressDialog;
import com.metricsfab.ubimp.shared.UbimpServiceSettingsManager;
import com.metricsfab.ubimpservice.R;
import com.metricsfab.ubimpservice.databinding.ActivityLoginBinding;
import com.metricsfab.utils.http.ApiResultBase;
import com.metricsfab.utils.http.RestApiClientGenerator;
import com.metricsfab.utils.permissions.PermissionsManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Clase encarcada de interactuar con el usuario para autenticar a el dispositivo
 */
public class Login extends AppCompatActivity implements SMSReceiveListener
{

    static final String EMAIL = "EMAIL_SETTING";

    static final int GET_COUNTRIES = 1;

    static final String GET_COUNTRIES_REST_URL = "countries/";

    static final String POST_DEVICES_ACTIVATE_REST_URL = "devices/activate/";

    static final int POST_LOGIN = 2;

    int accuracy;

    String appName;

    EditText countryCodeET;

    EditText emailTV;

    int executedAPIRestOperation;

    long fastest_update_interval;

    /**
     * Indica el protocolo
     */
    String webProtocol;

    /**
     * Indica el nombre del host para hacer las llamadas rest api
     */
    String webHostname;

    /**
     * Indica el puerto de
     */
    String webPort;

    /**
     * Indica la url base del servidor que contiene los endpoints de los servicios rest api
     */
    String webBaseUrl;

    /**
     * IMEI
     */
    String imei;

    /**
     * Identificador del pais
     */
    String countryId;

    int locationServiceTypeClass;

    AppCompatButton loginButton;
    String mPhoneNumber;
    EditText passwordTV;
    EditText phoneNumberTV;

    //ProgressDialog progressDialog;

    private boolean readPhoneStatePermissionGranted;

    int responseCode;

    String responseMessage;

    String responseUserMessage;

    String settingsName;

    private boolean smsReceivePermissionGranted;

    /**
     * Recibidor de mensajes sms
     */
    SMSReceiver smsReceiver;

    /**
     * Ultima marca de tiempp para activar el dispositivo en cadena. Cuando el dispositivo se esta activando
     * en esta variable se guarda la marca de tiempo cuando se envia hacia el servidor la peticion de activiacion
     * cuando se recibe el mensaje sms de activiacion, se busca en el sms esta marca de tiempo
     */
    String lastTimeStampForActivate;

    /**
     * Cuando el dispositivo se esta activiando y el servidor regresa la llamada de activacion correctamente
     * en el cuerpo de la respuesta del post ahi debe de venir el codigo de activacion generado por el servidor
     * este mismo codigo se buscara en el mensaje de sms que se reciba para activacion
     */
    String lastVerificationCodeReceived;

    int source;

    Spinner spinnerCountries;

    int tcpPort;

    double timeBetweenSendingTcpDataInMiliseconds;

    String timeStampString;

    long update_interval;

    String verificationCode;

    int verificationCodeLength;

    LoginService loginService;

    /**
     * Listado de paises
     */
    List<Country> countries;

    ActivityLoginBinding viewBinding;

    /**
     * Cuadro de dialogo de progreso de actividad
     */
    ProgressDialog progressDialog;

    /**
     * Funciones////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * Inicializa las variables globales de acuerdo a el bundle inicial. Generalmente este bundle proviene del activity Main
     * @param initBundle bundle inicial
     */
    private boolean setGloblalVariables(Bundle initBundle)
    {
        if (initBundle != null)
        {
            this.appName        = initBundle.getString("APP_NAME");
            this.settingsName   = initBundle.getString(UbimpServiceSettingsManager.UBIMP_SERVICE_SETTINGS_LABEL);

            this.webHostname    = initBundle.getString(UbimpServiceSettingsManager.WEB_HOSTNAME_LABEL);
            this.webPort        = initBundle.getString(UbimpServiceSettingsManager.WEB_PORT_LABEL);
            this.webProtocol    = initBundle.getString(UbimpServiceSettingsManager.WEB_PROTOCOL_LABEL);
            this.tcpPort        = initBundle.getInt(UbimpServiceSettingsManager.TCP_PORT_LABEL);

            this.update_interval            = initBundle.getLong("UPDATE_INTERVAL");
            this.fastest_update_interval    = initBundle.getLong("FASTEST_UPDATE_INTERVAL");
            this.accuracy                   = initBundle.getInt("ACCURACY");
            this.locationServiceTypeClass   = initBundle.getInt("LOCATION_SERVICE_TYPE_CLASS");
            this.timeBetweenSendingTcpDataInMiliseconds = initBundle.getInt("TIME_BETWEEN_SENDING_TCP_DATA_IN_MILISECONDS");
            this.webBaseUrl = this.webProtocol + "://" + this.webHostname + ":" + this.webPort;
            return true;
        }

        return false;

    }

    /**
     * Verifica si el programa tiene permiso para recibir sms, sino, lo solicita al usuario
     * @param rationaleMessageId Id del mensaje en resources para mostrarle al usuario en caso de
     * solicitar el permiso
     */
    private void checkReceiveSMSPermission(int rationaleMessageId)
    {
        if (!PermissionsManager.checkAccessPermission(getApplicationContext(), PermissionsManager.RECEIVE_SMS_PERMISSION_LABEL))
        {
            PermissionsManager.requestPermissions(this, this.findViewById(R.id.drawer_layout), PermissionsManager.RECEIVE_SMS_PERMISSION_LABEL, PermissionsManager.RECEIVE_SMS_PERMISSION_REQUEST_CODE, rationaleMessageId, this.appName, 0);
        }
    }


    /**
     *
     * @param rationaleMessageId
     * @return
     */
    private void checkReadPhoneStateAndSetImei(int rationaleMessageId)
    {
        if (!PermissionsManager.checkAccessPermission(getApplicationContext(), PermissionsManager.READ_PHONE_STATE_PERMISSION_LABEL))
        {
            PermissionsManager.requestPermissions(this, this.findViewById(R.id.drawer_layout),PermissionsManager.READ_PHONE_STATE_PERMISSION_LABEL, PermissionsManager.READ_PHONE_STATE_PERMISSION_REQUEST_CODE, rationaleMessageId, this.appName, 0);
        }
        else
        {
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= 26)
            {
                this.imei = telephonyManager.getImei();
            }
            else
            {
                this.imei = telephonyManager.getDeviceId();
            }
        }
    }



    /**
     * Muestra el cuadro de dialogo de progreso
     * @param initStatusMessage
     */
    private void showProgressDialog(String initStatusMessage)
    {
        if(progressDialog == null)
        {
            progressDialog = new ProgressDialog(Login.this);
        }

        progressDialog.showProgressDialog(false);
        progressDialog.setStatusText(initStatusMessage);

    }


    /**
     *
     * @param title titulo del cuadro del mensaje
     * @param message mensaje para mostrar en el cuadro de mensaje
     * @param cancelButtonMessage etiqueta para mostrar en el boton cancelar
     */
    private void showMessageDialog(String title, String message, String cancelButtonMessage, boolean cancellable, boolean addOkButton, boolean addCancelButton, final OnOkDialogButtonAction onOkAction)
    {
        // Se crea el constructor del cuadro de dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Se establece el mensaje y el titulo
        builder.setMessage(message).setTitle(title);


        if(addOkButton)
        {
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (onOkAction)
                    {
                        case initMainActivity:
                            finishAndStartMainActivity();
                            break;
                    }
                }
            });
        }

        if(addCancelButton)
        {
            builder.setNegativeButton(cancelButtonMessage, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(cancellable);
        dialog.show();
    }



    /**
     * Crea las instancias de las variables globales
     */
    private void instanciateGlobalVariables()
    {
        this.countries = new ArrayList<Country>();
    }

    /**
     * Establece los eventos de los widgets de UI
     * @param vb view binding de la vista
     */
    private void setLoginEvents(final ActivityLoginBinding vb, final LoginService loginService, final String imei)
    {

        // Evento click del botons
        vb.btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Se activa el cuadro de dialogo de progreso
                showProgressDialog(getString(R.string.activatingDeviceLabel));
                lastTimeStampForActivate = Long.valueOf(System.currentTimeMillis() / 1000L).toString();
                // Se crea el commando para activar el dispositivo
                String lang = Locale.getDefault().toString();
                ActivateDeviceCommand activateDeviceCommand = new ActivateDeviceCommand(imei, vb.inputEmail.getText().toString(), vb.inputPassword.getText().toString(), vb.inputPhoneNumber.getText().toString(), countryId,  lastTimeStampForActivate, lang);
                // loginService.activateDevice(activateDeviceCommand).enqueue(setActivateDeviceCallback());

            }
        });



    }


    /**
     * Establece el evento onItemSelectedListener a un spinner
     * @param spinner
     */
    private void setOnItemSelectedListener(Spinner spinner, final TextView textView)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> param1AdapterView, View param1View, int param1Int, long param1Long)
            {
                textView.setText(countries.get(param1Int).PhoneCode);
                countryId = countries.get(param1Int).CountryId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
    }

    /**
     *
     * @param spinner
     * @param itemList
     */
    private void setAdapterToSpinner(Spinner spinner, ArrayList<String> itemList)
    {

        //Se instancia el adaptador del arreglo de cadenas
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.spinner_item, itemList);

        //Se establece el layout del dropdown para el adaptador
        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        if (spinner == null)
        {
            Log.d(this.appName, "Spinner Countries null");
        }
        try
        {
            spinner.setAdapter(arrayAdapter);
        }
        catch (Exception exception)
        {
            Log.d(this.appName, exception.getMessage());
        }
    }


    /**
     * Guarda la configuracion del dispositivo
     */
    public void saveSettings()
    {
        SharedPreferences.Editor editor = (new UbimpServiceSettingsManager(getApplicationContext())).getEditor();
        editor.putBoolean(UbimpServiceSettingsManager.REGISTERED_VALUE_LABEL, true);
        editor.putString(UbimpServiceSettingsManager.WEB_HOSTNAME_LABEL, this.webHostname);
        editor.putString(UbimpServiceSettingsManager.WEB_PORT_LABEL, this.webPort);
        editor.putInt(UbimpServiceSettingsManager.TCP_PORT_LABEL, this.tcpPort);
        editor.putLong(UbimpServiceSettingsManager.UPDATE_INTERVAL_LABEL, this.update_interval);
        editor.putLong(UbimpServiceSettingsManager.FASTEST_UPDATE_INTERVAL_LABEL, this.fastest_update_interval);
        editor.putInt(UbimpServiceSettingsManager.ACCURACY_LABEL, this.accuracy);
        editor.putInt(UbimpServiceSettingsManager.LOCATION_SERVICE_TYPE_CLASS_LABEL, this.locationServiceTypeClass);
        editor.putBoolean(UbimpServiceSettingsManager.LOCATION_UPDATES_REQUEST_OF_THE_APP_LABEL, true);
        editor.putLong(UbimpServiceSettingsManager.TIME_BETWEEN_SENDING_TCP_DATA_IN_MILISECONDS_LABEL, Double.doubleToRawLongBits(this.timeBetweenSendingTcpDataInMiliseconds));
        editor.commit();
    }


    /**
     * Finaliza la actividad Login, inicia la actividad principal
     */
    private void finishAndStartMainActivity()
    {
        // Se inicia la actividad principal
        startActivity(new Intent(this, MainActivity.class));

        // Se termina la actividad login
        finish();
    }


    // Eventos /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Se obtiene el view binding autogenerado
        this.viewBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        // Se obtiene el widget raiz
        View view = this.viewBinding.getRoot();
        // Se establece el contenido
        setContentView(view);

        // En caso de que se quiera establecer el contenido directament por el layout habilitar
        // esta sentencia y comentar las 3 sentencias del view binding
        //setContentView(R.layout.activity_login);

        Bundle initBundle = getIntent().getExtras();
        // Si las variables no se establecen con exito, entonces se finaliza la aplicacion
        if(!this.setGloblalVariables(initBundle)) finish();

        this.checkReceiveSMSPermission(R.string.receive_sms__permission_rationale);

        // this.checkReadPhoneStateAndSetImei(R.string.phone_state_permission_rationale);

        // Instancia las variables globales
        this.instanciateGlobalVariables();

        // Obtiene el callback de los paises. setCountriesCallback debe de tener la logica necesaria de lo que se hara
        // cuando la llamada hacia el servicio api termine o devuelva un error
        Callback<ApiResultBase> countriesCallBack = this.setCountriesCallback(viewBinding.spinnerCountries);
        // Se establece el evento onItemSelected
        this.setOnItemSelectedListener(viewBinding.spinnerCountries, viewBinding.countryCode);


        // Se instancia el servicio del login
        try {

            // Si se esta ejecutan en ambiente de desarrollo, se crea el servicio de login usando el certificado de pruebas del servidor. ES IMPORTANTE
            // SOLO USAR ESTA OPCION EN DESARROLLO
            if(UbimpServiceSettingsManager.env == "dev")
            {
                InputStream cert = getApplicationContext().getResources().openRawResource(R.raw.localhost);
                this.loginService = RestApiClientGenerator.createService(this.webBaseUrl, LoginService.class, cert);
            }
            else
            {

            }

            // Establece el evento del boton login
            this.setLoginEvents(viewBinding, this.loginService, "123456789");

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        // Se "encola" la llamada
         this.loginService.getCountries().enqueue(countriesCallBack);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

        // Se crea aqui el recibidor de sms para agilizar el recibimiento del sms cuando se termine
        // la llamada post
        smsReceiver = new SMSReceiver(appName);
        smsReceiver.setListener(Login.this);
        registerReceiver(smsReceiver, intentFilter);

    }

    /**
     * Establece el callback cuando se termine la llamada hacia el api de paises
     * @param countriesSpinner Spinner (dropdown) en donde se mostrara el listado de paises
     * @return nada
     */
    private Callback<ApiResultBase> setCountriesCallback(final Spinner countriesSpinner)
    {
        return new Callback<ApiResultBase>() {
            @Override
            public void onResponse(Call<ApiResultBase> call, Response<ApiResultBase> response) {
               ApiResultBase apiResultBase = response.body();
               ArrayList<LinkedTreeMap<String, String>> countriesResponse = (ArrayList<LinkedTreeMap<String, String>>) apiResultBase.data;
               ArrayList<String> countriesStrings = new ArrayList<String>();
               for(int i = 0; i < countriesResponse.size(); i++)
               {
                   countries.add(new Country(countriesResponse.get(i).get("name"), countriesResponse.get(i).get("phoneCode"), countriesResponse.get(i).get("countryId")));
                   countriesStrings.add(countriesResponse.get(i).get("name"));
               }

               setAdapterToSpinner(countriesSpinner, countriesStrings);
               viewBinding.inputPhoneNumber.requestFocus();
            }

            @Override
            public void onFailure(Call<ApiResultBase> call, Throwable throwable) {
                System.out.println(throwable);
            }
        };
    }


    /**
     * Establece la llamada que se invocara cuando la solicitud de activacion haya concluido
     * @return La llamada a invocarse cuando la solicitu de activacion haya concluido
     */
    private Callback<ApiResultBase> setActivateDeviceCallback() {

        return new Callback<ApiResultBase>() {
            @Override
            public void onResponse(Call<ApiResultBase> call, Response<ApiResultBase> response) {

                if( response.isSuccessful() == true)
                {
                    ApiResultBase apiResultBase = response.body();
                    if (apiResultBase != null)
                    {
                        // Si el resultado es exitoso
                        if(apiResultBase.isSuccess)
                        {
                            progressDialog.setStatusText(getString(R.string.waitingVerificationCodeLabel));
                            lastVerificationCodeReceived = (String) apiResultBase.data;
                        }
                        else // El resultado a nivel http fue exitoso, sin embargo a nivel aplicacion no fue exitoso, entonces
                        {   // el servidor envia el mensaje del error para el usuario
                            progressDialog.dismissDialog();
                            showMessageDialog(appName, apiResultBase.userMessage, getString(R.string.cancelLabel), false, true, false, OnOkDialogButtonAction.None);
                        }

                    }
                    else
                    {

                    }
                }
                else // La respuesta no es exitosa
                {
                    // Se oculta el cuadro de dialogo de progreso
                    progressDialog.dismissDialog();
                    showMessageDialog(appName, getString(R.string.errorOnActivatingLabel), getString(R.string.cancelLabel), false, true, false, OnOkDialogButtonAction.None);

                }

            }

            @Override
            public void onFailure(Call<ApiResultBase> call, Throwable throwable) {
                System.out.println(throwable);
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.smsReceiver);
    }

    /**
     *
     * @param fromNumber Numero que envio el mensaje
     * @param message mensaje
     */
    @Override
    public void onSmsReceived(String fromNumber, String message) {

        if (fromNumber != null && message != null)
        {
            // Aqui se verifica que el mensaje contenga la marca de tiempo enviada al server y el codigo de verificacion generado en el servidor,
            // ambos deben de estar en el mensaje sms
            if (message.contains(this.lastTimeStampForActivate) && message.contains(this.lastVerificationCodeReceived))
            {
                Log.d(this.appName, "El dispositivo se esta registrando...");
                this.saveSettings();
                // Se detiene el cuadro de dialo de progreso
                this.progressDialog.dismissDialog();

                showMessageDialog(appName, getString(R.string.deviceActivatedSuccessfullyLabel), getString(R.string.cancelLabel), false, true, false, OnOkDialogButtonAction.initMainActivity);

                Log.d(this.appName, "El dispositivo se ha registrado correctamente");
            }
            else
            {
                Log.d(this.appName, "Error");
                // Se detiene el cuadro de dialo de progreso
                this.progressDialog.dismissDialog();

            }
        }
    }
}
