package com.metricsfab.utils.permissions;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

public class PermissionsManager
{
    public static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 100;

    public static final int RECEIVE_SMS_PERMISSION_REQUEST_CODE          = 200;

    public static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE     = 300;

    public static final String RECEIVE_SMS_PERMISSION_LABEL = "android.permission.RECEIVE_SMS";

    public static final String READ_PHONE_STATE_PERMISSION_LABEL = "android.permission.READ_PHONE_STATE";

    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";

    public static boolean checkAccessFineLocationPermissions(Context paramContext)
    {
        boolean bool;
        if (ActivityCompat.checkSelfPermission(paramContext, "android.permission.ACCESS_FINE_LOCATION") == 0)
        {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }


    /**
     *
     * @param paramContext
     * @param paramString
     * @return
     */
    public static boolean checkAccessPermission(Context paramContext, String paramString)
    {
        boolean bool;
        if (ActivityCompat.checkSelfPermission(paramContext, paramString) == 0) {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }


    /**
     *
     * @param paramContext
     * @return
     */
    public static boolean checkAccessReadPhoneStatePermissions(Context paramContext)
    {
        boolean bool;
        if (ActivityCompat.checkSelfPermission(paramContext, "android.permission.READ_PHONE_STATE") == 0)
        {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }


    /**
     *
     * @param activity
     * @param view
     * @param permission
     * @param request_permissions_request_code
     * @param rationaleMessageResourceId
     * @param appName
     * @param okResourceId
     */
    public static void requestPermissions(final Activity activity, View view, final String permission, final int request_permissions_request_code, int rationaleMessageResourceId, String appName, int okResourceId)
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
        {
            Log.i(appName, "Mostrando la justificacion para proveer el contexto de porque se debe de conceder el permiso");

            Snackbar.make(view, rationaleMessageResourceId, Snackbar.LENGTH_INDEFINITE).setAction(okResourceId, new View.OnClickListener()
            {
                public void onClick(View param1View)
                {
                    ActivityCompat.requestPermissions(activity, new String[] { permission }, request_permissions_request_code);
                }

            }).addCallback(new Snackbar.Callback()
            {
                public void onDismissed(Snackbar param1Snackbar, int param1Int)
                {

                }

                public void onShown(Snackbar param1Snackbar)
                {


                }

            }).show();
        }
        else
        {
            Log.i(appName, "Requesting permission");

            ActivityCompat.requestPermissions(activity, new String[] { permission }, request_permissions_request_code);
        }
    }
}

