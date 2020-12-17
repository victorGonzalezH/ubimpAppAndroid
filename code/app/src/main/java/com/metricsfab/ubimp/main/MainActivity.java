package com.metricsfab.ubimp.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.metricsfab.ubimp.shared.UbimpServiceSettingsManager;
import com.metricsfab.ubimp.login.Login;
import com.metricsfab.ubimp.service.LocationService;
import com.metricsfab.ubimpservice.R;
import com.metricsfab.utils.permissions.PermissionsManager;
import com.metricsfab.utils.playServices.IDialogCancelListener;
import com.metricsfab.utils.ui.IOnEventListener;

public class MainActivity extends AppCompatActivity implements
        IDialogCancelListener,
        NavigationView.OnNavigationItemSelectedListener,
        IOnEventListener
{


    //Propiedades
    private boolean accessFineLocationPermission;

    private int accuracy;

    private String appName;

    private Fragment currentFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.locationUpdatesRequestOfTheApp = false;
        this.accessFineLocationPermission   = false;

        try
        {
            //Se obtiene la version del paquete de servicios de google
            //El valor de cero en la funcion getPackageInfo
            this.playServiceVersion = PackageInfoCompat.getLongVersionCode(getPackageManager().getPackageInfo(UbimpServiceSettingsManager.PLAY_SERVICES_PACKAGE_NAME, 0));

            //Si el valor es cero (valor devuelto cuando el paquete no esta instalado)
            if (this.playServiceResult == 0)
            {
                SetAndConfigureUI(this);

                UbimpServiceSettingsManager ubimpServiceSettingsManager = new UbimpServiceSettingsManager(getApplicationContext());

                //Se obtiene la bandera que indica si el dispositivo esta registrado o no
                this.registered = ubimpServiceSettingsManager.isDeviceRegistered();

                //Si el dispositivo esta registrado
                if (this.registered)
                {
                    this.locationUpdatesRequestOfTheApp = ubimpServiceSettingsManager.isLocationUpdateRequestOfTheAppEnabled();
                    this.updateInterval = ubimpServiceSettingsManager.getUpdateInterval(10000L);
                    this.fastestUpdateInterval = ubimpServiceSettingsManager.getFastestUpdateInterval(5000L);
                    this.accuracy = ubimpServiceSettingsManager.getAccuracy(100);

                    if (this.locationUpdatesRequestOfTheApp)
                        if (!PermissionsManager.checkAccessFineLocationPermissions(getApplicationContext()))
                        {
                            this.requestingFineLocationAccess = true;

                            PermissionsManager.requestPermissions(this, this.findViewById(R.id.drawer_layout), PermissionsManager.ACCESS_FINE_LOCATION, PermissionsManager.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE, R.string.accessFineLocationRationale, this.appName, 0);
                            //this, this.findViewById(R.id.drawer_layout), "android.permission.ACCESS_FINE_LOCATION", 100, 2131230794, 2131624020, "Ubimp Service"
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


    @Override
    public void onEventSwitch(boolean param1Boolean)
    {

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

     */
    public void SetAndConfigureUI(Activity paramActivity)
    {

        //Obtiene el objeto toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(paramActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener)paramActivity);
        navigationView.getMenu().getItem(0).setChecked(true);


        MainFragment mainFragment = new MainFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.relative_layout_for_fragment, mainFragment, mainFragment.getTag()).commit();

        this.currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFragment);

    }
}
