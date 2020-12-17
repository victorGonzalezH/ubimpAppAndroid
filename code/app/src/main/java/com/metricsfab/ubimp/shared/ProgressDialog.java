package com.metricsfab.ubimp.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.metricsfab.ubimpservice.R;


public class ProgressDialog {


    /**
     * Actividad padre
     */
    private Activity parentActivity;


    /**
     * Cuadro de dialogo
     */
    private AlertDialog alertDialog;


    /**
     *
     * @param parentActivity Actividad padre
     */
    public ProgressDialog(Activity parentActivity)
    {
        this.parentActivity = parentActivity;

    }

    private  String initialStatusMessage;


    /**
     *
     * @param cancellable Indica si el cuadro de dialogo es cancelable
     */
    public void showProgressDialog(boolean cancellable)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.parentActivity);
        LayoutInflater inflater = this.parentActivity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_dialog, null));
        builder.setCancelable(cancellable);
        alertDialog = builder.create();
        alertDialog.show();
    }




    public void dismissDialog()
    {
        this.alertDialog.dismiss();
    }


    /**
     * Establece el texto del estatus
     * @param statusText
     */
    public void setStatusText(String statusText)
    {
        TextView tvStatus = alertDialog.findViewById(R.id.textViewStatus);
        tvStatus.setText(statusText);
    }

}
