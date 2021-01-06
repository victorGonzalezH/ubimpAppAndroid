package com.metricsfab.ubimp.main;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.metricsfab.ubimp.shared.UbimpServiceSettingsManager;
import com.metricsfab.ubimp.login.Login;
import com.metricsfab.ubimp.service.LocationService;
import com.metricsfab.ubimp.shared.models.LocationData;
import com.metricsfab.ubimpservice.R;
import com.metricsfab.utils.permissions.PermissionsManager;
import com.metricsfab.utils.playServices.IDialogCancelListener;
import com.metricsfab.utils.playServices.PlayServicesUtils;
import com.metricsfab.utils.ui.IOnEventListener;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        IDialogCancelListener,
        NavigationView.OnNavigationItemSelectedListener,
        IOnEventListener
{


    //Propiedades
    private boolean accessFineLocationPermission;

    private int accuracy;

    private String appName;

    private long fastestUpdateInterval;

    private LocationService locationService;


    //Bandera que indica si las solicitudes para obtener las ubicaciones estan activas o no
    //Esta configuracion la puede cambiar solo el usuario
    private boolean locationUpdatesRequestOfTheApp;

    private int playServiceResult;

    private long playServiceVersion;

    private boolean playServicesUpdating;

    private boolean registered;

    private boolean requestingFineLocationAccess;

    private boolean serviceBounded;

    private long updateInterval;

    private int tcpPort;

    private String tcpHost;

    private double imei;

    /**
     * Objeto administrador de las configuraciones de la aplicacion
     */
    UbimpServiceSettingsManager ubimpServiceSettingsManager;

    /**
     * Mensajero para recibir mensjaes del servicio
     */
    Messenger messengerToReceiveMessagesFromService;

    /**
     * Indica si la actividad se encuentra o no enlazada con el servicio
     */
    boolean boundToService;

    /**
     * Mensajero para enviar mensajes hacia el servicio
     */
    Messenger messengerToSendMessagesToService;


    AppBarConfiguration appBarConfiguration;

    /**
     * Barra de navegacion lateral (cajon)
     */
    DrawerLayout drawerLayout;


    /**
     * Controlador de navegacion
     */
    NavController navController;

    /**
     * Host Fragment
     */
    NavHostFragment navHostFragment;

    /**
     * Indica el id del fragment actual
     */
    private int currentFragmentId;

    /**
     * Defines las llamadas de vuelta cuando se hace la conexion con el servicio
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {

            Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
            messengerToSendMessagesToService = new Messenger(service);
            boundToService = true;
            // Se envia un mensaje inicial a el servicio, esto con el objetivo de que guarde el mensajero
            // de esta actividad principal y posteriormente lo use para notificar a esta actividad sobre
            // los eventos que suceden en el
            senMessageToService(1, messengerToReceiveMessagesFromService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            messengerToSendMessagesToService = null;
            boundToService = false;
        }
    };


    /**
     * Funciones////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */


    /**
     *
     * @param messageId Mensaje
     * @param replyMessenger Mensajero al que se le va a responder
     */
    void senMessageToService(int messageId, Messenger replyMessenger)
    {
        Message msg = Message.obtain(null, messageId, 0, 0);
        msg.replyTo = replyMessenger;

        try
        {
            messengerToSendMessagesToService.send(msg);
        } catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Inicia el servicio de localizacion
     */
    private void startLocationService()
    {
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra(UbimpServiceSettingsManager.APP_NAME_LABEL, this.appName);
        intent.putExtra(UbimpServiceSettingsManager.UPDATE_INTERVAL_LABEL, this.updateInterval);
        intent.putExtra(UbimpServiceSettingsManager.FASTEST_UPDATE_INTERVAL_LABEL, this.fastestUpdateInterval);
        intent.putExtra(UbimpServiceSettingsManager.ACCURACY_LABEL, this.accuracy);
        intent.putExtra(UbimpServiceSettingsManager.TCP_HOSTNAME_LABEL, this.tcpHost);
        intent.putExtra( UbimpServiceSettingsManager.TCP_PORT_LABEL, this.tcpPort);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
        else
        {
            startService(intent);
        }

        // Se enlaza a el servicio
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.locationUpdatesRequestOfTheApp = false;

        this.accessFineLocationPermission   = false;

        // Se inicializa el manejador de mensajes
        this.messengerToReceiveMessagesFromService = new Messenger(new MainActivityHandler());

        try
        {
            //Se obtiene la version del paquete de servicios de google
            //El valor de cero en la funcion getPackageInfo
            this.playServiceVersion = PackageInfoCompat.getLongVersionCode(getPackageManager().getPackageInfo(UbimpServiceSettingsManager.PLAY_SERVICES_PACKAGE_NAME, 0));

            //Si el valor es cero (valor devuelto cuando el paquete no esta instalado)
            if (this.playServiceResult == 0)
            {
                SetAndConfigureUI(this);

                ubimpServiceSettingsManager = new UbimpServiceSettingsManager(getApplicationContext());

                //Se obtiene la bandera que indica si el dispositivo esta registrado o no
                this.registered = ubimpServiceSettingsManager.isDeviceRegistered();

                // SOLO PARA FINES DE PRUEBAS <--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                this.registered = true;

                //Si el dispositivo esta registrado
                if (this.registered)
                {
                    this.tcpHost = ubimpServiceSettingsManager.getHostNameFromSettings("10.0.2.2");
                    this.tcpPort = ubimpServiceSettingsManager.getTcpPortFromSettings(49371);
                    this.imei = ubimpServiceSettingsManager.getImeiDouble();

                    // Obtiene si el usuario ha activado el servicio, es decir en terminos del programa que haya
                    // deslizado el slider para activar el servicio
                    this.locationUpdatesRequestOfTheApp = ubimpServiceSettingsManager.isLocationUpdateRequestOfTheAppEnabled();
                    this.updateInterval = ubimpServiceSettingsManager.getUpdateInterval(10000L);
                    this.fastestUpdateInterval = ubimpServiceSettingsManager.getFastestUpdateInterval(5000L);
                    this.accuracy = ubimpServiceSettingsManager.getAccuracy(100);
                    this.locationUpdatesRequestOfTheApp = ubimpServiceSettingsManager.isLocationUpdateRequestOfTheAppEnabled();



                    // Dado que el dispositivo ya esta activado, es neesario que la aplicacion cuente con el permiso de acceder
                    // a la ubicacion de manera preciza, por eso se le pide a el usuario concederlos
                    if (!PermissionsManager.checkAccessFineLocationPermissions(getApplicationContext()))
                    {
                        this.requestingFineLocationAccess = true;

                        PermissionsManager.requestPermissions(this, this.findViewById(R.id.drawer_layout), PermissionsManager.ACCESS_FINE_LOCATION, PermissionsManager.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE, R.string.accessFineLocationRationale, this.appName, 0);

                    }
                    else
                    {
                        this.accessFineLocationPermission = true;
                        this.requestingFineLocationAccess = false;
                    }
                }
                else
                {
                    Log.d(this.appName, "Device not registered");
                    startActivity(setLoginItent(getApplicationContext(), Login.class));
                    finish();
                }
            }
            else if (this.playServiceResult == 1)
            {
                Log.d(this.appName, "Google play services not available, user is updating");
            }

        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }


        if (this.playServicesUpdating == true && !PlayServicesUtils.isShowingErrorDialog())
        {
            if (this.playServiceVersion == PlayServicesUtils.getPlayServicesVersion(getApplicationContext()))
            {
                Log.d("Ubimp Service", "Finalizando la actividad dado que no se actualizo Google play services");
                finish();

            }

            Log.d("Ubimp Service", "Recreando la actividad dado que si se actualizo Google play services");
            this.playServicesUpdating = false;
            this.playServiceResult = 0;
            recreate();
        }
    }

    /**
     *
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        // Se pregunta si el dispositivo esta registrado
        if (this.registered == true)
        {
            // Si el servicio esta iniciado, solo se enlaza al servicio
            if (LocationService.ServiceStarted)
            {
                Intent intent = new Intent(this, LocationService.class);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
            else
            {
                startLocationService();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        return false;
    }


    @Override
    public void OnDialogCancel()
    {

    }


    public void onEventSwitch(boolean enableLocation)
    {
        SharedPreferences.Editor editor = this.ubimpServiceSettingsManager.getEditor();
        editor.putBoolean(UbimpServiceSettingsManager.LOCATION_UPDATES_REQUEST_OF_THE_APP_LABEL, enableLocation);



        // Si se activa obtener la ubicacion
        if(enableLocation == true)
        {
            // Si el servicio  esta iniciado y enlazado
            if(LocationService.ServiceStarted == true && boundToService == true)
            {
                // Envia el mensaje para empezar a recibir las actualizacion de ubicacion
                this.senMessageToService(2, this.messengerToReceiveMessagesFromService);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Funciones

    /*
        Establece el intent para el login.
     */
    public Intent setLoginItent(Context paramContext, Class<?> paramClass)
    {
        Intent intent = new Intent(paramContext, paramClass);
        intent.putExtra(UbimpServiceSettingsManager.APP_NAME_LABEL, this.appName);
        intent.putExtra(UbimpServiceSettingsManager.UBIMP_SERVICE_SETTINGS_LABEL, UbimpServiceSettingsManager.UBIMP_SERVICE_SETTINGS_VALUE);
        intent.putExtra(UbimpServiceSettingsManager.WEB_PROTOCOL_LABEL, UbimpServiceSettingsManager.WEB_PROTOCOL);
        intent.putExtra(UbimpServiceSettingsManager.WEB_HOSTNAME_LABEL, UbimpServiceSettingsManager.WEB_HOSTNAME);
        intent.putExtra(UbimpServiceSettingsManager.WEB_PORT_LABEL, UbimpServiceSettingsManager.WEB_PORT);


        intent.putExtra(UbimpServiceSettingsManager.TCP_PORT_LABEL, UbimpServiceSettingsManager.TCP_PORT);
        intent.putExtra("UPDATE_INTERVAL", 10000L);
        intent.putExtra("FASTEST_UPDATE_INTERVAL", 5000L);
        intent.putExtra("ACCURACY", 100);
        intent.putExtra("LOCATION_SERVICE_TYPE_CLASS", 1);
        intent.putExtra("TIME_BETWEEN_SENDING_TCP_DATA_IN_MILISECONDS", 500);
        return intent;
    }

    /*
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

     */

    /**
     *
     * @param paramActivity
     */
    public void SetAndConfigureUI(Activity paramActivity)
    {



        // ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(paramActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // drawerLayout.addDrawerListener(actionBarDrawerToggle);
        //actionBarDrawerToggle.syncState();


        //navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener)paramActivity);
        //navigationView.getMenu().getItem(0).setChecked(true);


        // MainFragment mainFragment = new MainFragment();
        // getSupportFragmentManager().beginTransaction().replace(R.id.relative_layout_for_fragment, mainFragment, "mainFragmentTag").commit();

        // Se obtiene el host de los fragments.
        navHostFragment =  (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        // Se obtiene el controlador de navegacion
        navController = navHostFragment.getNavController();

        // Se agrega evento para escuchar el cambio de fragment
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {

            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments)
            {
                currentFragmentId = destination.getId();
            }

        });


        final NavigationView navView = (NavigationView)findViewById(R.id.nav_view);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                int id = menuItem.getItemId();
                //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
                //if (id == R.id.nav_home)
                //{
                //    Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                //}


                //This is for maintaining the behavior of the Navigation view
                NavigationUI.onNavDestinationSelected(menuItem, navController);

                // Se cierra el cajon (drawer), despues de selccionar el item
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        final Set<Integer> topLevelDestinations = new ArraySet<>();
        topLevelDestinations.add(R.id.mainFragment);
        topLevelDestinations.add(R.id.settings);

        this.appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).setDrawerLayout(drawerLayout).build();

        //Obtiene el objeto toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration );
        navView.setCheckedItem(R.id.mainFragment);
        navController.navigate(R.id.mainFragment);


    }


    public void OnNewLocationArrive(LocationData locationData)
    {
        // Se obtiene
        if(currentFragmentId == R.id.mainFragment)
        {
            MainFragment mainFragment = (MainFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
            if(mainFragment != null)
            {
                mainFragment.setLocationImage(R.drawable.gps_fixed);
                mainFragment.setLocationText(R.string.locationAvailableLabel);
                mainFragment.setLocation(locationData.getLatitude(), locationData.getLongitude(), locationData.getSpeed());
            }
        }
    }


    class MainActivityHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what)
            {
                // El mensaje indica una nueva ubicacion
                case LocationService.OP_NEW_LOCATION_ARRIVE:
                    OnNewLocationArrive((LocationData) message.obj);
                    break;
            }
        }
    }


}
