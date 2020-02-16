package com.metricsfab.utils.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NoticeDialogFragment extends DialogFragment
{
    NoticeDialogListener mListener;

    String message;

    String title;

    public void onAttach(Context paramContext)
    {
        super.onAttach(paramContext);
        try
        {
            this.mListener = (NoticeDialogListener)paramContext;
            return;

        } catch (ClassCastException classCastException)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(paramContext.toString());
            stringBuilder.append(" must implement NoticeDialogListener");
            throw new ClassCastException(stringBuilder.toString());
        }
    }


    public Dialog onCreateDialog(Bundle paramBundle)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(this.title).setMessage(this.message).setPositiveButton(2131624012, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface param1DialogInterface, int param1Int)
            {
                Log.d("GeolocationService", "OKInsideDialog");
                NoticeDialogFragment.this.mListener.onDialogPositiveClick(NoticeDialogFragment.this);
            }
        });

        return builder.create();
    }

    public void setMessage(String paramString) { this.message = paramString; }

    public void setTitle(String paramString) { this.title = paramString; }

    public static interface NoticeDialogListener
    {
        void onDialogNegativeClick(DialogFragment param1DialogFragment);

        void onDialogPositiveClick(DialogFragment param1DialogFragment);
    }
}

