package com.metricsfab.ubimp.service;

import android.os.Message;

/**
 * Interface para reenviar los mensajes que se reciben los mensajes
 */
public interface MessengerListener {
    public void forwardMessage(Message message);
}

