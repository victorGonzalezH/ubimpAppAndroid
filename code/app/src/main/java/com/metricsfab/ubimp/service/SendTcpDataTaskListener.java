package com.metricsfab.ubimp.service;

import com.metricsfab.ubimp.models.LocationData;
import com.metricsfab.utils.net.TcpClient;

import java.util.concurrent.BlockingQueue;

public interface SendTcpDataTaskListener
{
    public void onSendTcpDataProgressUpdate(int bytesSent);
    public void onSendTcpDataPostExecute(int totalBytesSent);
    public String getApplicationName();
    public BlockingQueue<LocationData> getLocationQueue();
    public TcpClient getTcpClient();
    public void onTcpClientDisconnected();

}
