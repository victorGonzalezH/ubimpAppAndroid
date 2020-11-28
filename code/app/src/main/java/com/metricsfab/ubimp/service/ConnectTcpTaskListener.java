package com.metricsfab.ubimp.service;

/**
 * Interface que usa la tarea ConnectTcpClientTask para notificar de los eventos a al objeto que
 * lo instancia
 */
public interface ConnectTcpTaskListener {

    public void setConnectingFlag(boolean connecting);
    public String getApplicationName();
    public void onDoInBackground();
    public  void onConnectTcpClientTaskResult(boolean connectResult);
}
