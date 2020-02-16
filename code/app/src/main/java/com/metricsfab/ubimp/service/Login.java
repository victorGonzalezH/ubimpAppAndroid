package com.metricsfab.ubimp.service;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;


import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.metricsfab.ubimpservice.R;

import java.util.ArrayList;

public class Login extends AppCompatActivity
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

    String hostname;

    int httpsPort;

    String imei;

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

    //SMSReceiver smsReceiver;

    int source;

    Spinner spinnerCountries;

    int tcpPort;

    double timeBetweenSendingTcpDataInMiliseconds;

    String timeStampString;

    long update_interval;

    String verificationCode;

    int verificationCodeLength;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }



    ////////////////////////////////////////////////////////////////////////////
    //Funciones
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

}
