package com.metricsfab.utils.playServices;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.android.gms.common.GoogleApiAvailability;

public class PlayServicesUtils
{
    public static final int PLAY_SERVICE_UTIL_ERROR = -1;

    public static final int PLAY_SERVICE_UTIL_SUCCESS = 0;

    public static final int PLAY_SERVICE_UTIL_USER_RESOLVE = 1;

    private static Dialog errorDialog = null;



    public static int checkPlayServices(final Activity activity, Context context, int paramInt)
    {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int i = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (i != 0)
        {
            if (googleApiAvailability.isUserResolvableError(i))
            {
                errorDialog = googleApiAvailability.getErrorDialog(activity, i, paramInt, new DialogInterface.OnCancelListener()
                {
                    public void onCancel(DialogInterface param1DialogInterface) { ((IDialogCancelListener)activity).OnDialogCancel(); }
                });

                Dialog dialog = errorDialog;

                if (dialog != null)
                {
                    dialog.show();

                    return PLAY_SERVICE_UTIL_USER_RESOLVE;
                }
            }

            return PLAY_SERVICE_UTIL_ERROR;
        }

        return PLAY_SERVICE_UTIL_SUCCESS;
    }




    public static int getPlayServicesVersion(Context paramContext)
    {
        return GoogleApiAvailability.getInstance().getClientVersion(paramContext);
    }


    public static boolean isPlayServicesPossiblyUpdating(Context paramContext)
    {
        return GoogleApiAvailability.getInstance().isPlayServicesPossiblyUpdating(paramContext, 1);
    }



    public static boolean isShowingErrorDialog()
    {
        boolean bool;
        Dialog dialog = errorDialog;
        if (dialog != null && dialog.isShowing())
        {
            bool = true;
        }
        else
            {
            bool = false;
        }

        return bool;
    }

}