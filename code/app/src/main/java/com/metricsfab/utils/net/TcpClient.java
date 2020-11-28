package com.metricsfab.utils.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient
{
    public static final int CONNECTED_SUCCESS = 0;

    public static final int SEND_SUCCESS = 0;

    public static final int SEND_ERROR = 0;

    public static final int INVALID_SERVER_OR_PORT = 1;

    public static final int ERROR_EXCEPTION_ON_CONNECT = 2;


    public boolean ShowMessageFromServerOnToast;

    private String appName;


    //Stream o flujo para escribir
    private  OutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;

    //Buffer o almacenamiento temporal de memoria para escribir
    private BufferedWriter bufferedWriter;


    private PrintWriter bufferWriter;


    //////////////////////////////////////////////////////////
    //Variables para la lectura de los datos en el socket de conexion
    //Stream o flujo de datos para leer
    private InputStreamReader inputStreamReader;

    //Buffer o almacenamiento temporal de memoria para leer datos
    private BufferedReader bufferedReader;



    private boolean isBrokenPipe;

    private boolean isClientConnected;

    private boolean isEnabledListenMessages;

    private ITcpMessageListener listener;

    private String messageReceived;

    private int sendMessageFailureCounter;

    public String hostName;

    private Socket socket;

    public int port;


    /*
        Constructor de la clase
     */
    public TcpClient(String hostName, int port, ITcpMessageListener tcpMessageListener, String appName)
    {
        this.hostName = hostName;
        this.port       = port;
        this.listener   = tcpMessageListener;
        this.appName    = appName;

        //Indica si el cliente esta conectado o no
        this.isClientConnected      = false;
        //Indica si la conexion esta establecida o no
        this.isBrokenPipe           = false;

        this.sendMessageFailureCounter = 0;
    }



    public int connect(String hostName, int port, boolean debug) throws IOException
    {
        try
        {
            this.hostName = hostName;
            this.port = port;
            return this.connect();
        }
        catch (Exception ex)
        {
            throw  ex;
        }

    }


    public int connect() throws IOException
    {
        try
        {
            if (this.hostName != "" && this.port > 0)
            {
                InetAddress inetAddress = InetAddress.getByName(this.hostName);
                Log.d(appName, "Triying to connect to TCP server");
                this.socket = new Socket(inetAddress, this.port);

                //Dado que la conexion hacia el servidor TCP es basicamente la creacion correcta
                //del socket, es por eso que la instanciacion de los objetos inputStreamReader
                //y outputStreamWriter se crean en este metodo ya que dependen de la creacion
                //correcta del socket

                this.inputStreamReader = new InputStreamReader(this.socket.getInputStream());
                //this.bufferedReader = new BufferedReader(inputStreamReader);


                this.outputStream   = this.socket.getOutputStream();

                //bufferedWriter     = new BufferedWriter(outputStreamWriter);
                //PrintWriter printWriter = new PrintWriter( );

                this.isClientConnected = true;
                this.isBrokenPipe      = false;

                Log.d(appName, "TCP client is connected");

                return CONNECTED_SUCCESS;
            }

            Log.d(appName, "Invalid server name or tcp port");

            return INVALID_SERVER_OR_PORT;
        }
        catch (Exception exception)
        {
            Log.d(this.appName, exception.getMessage());
            this.isBrokenPipe       = true;
            this.isClientConnected  = false;
            throw  exception;
        }
        finally
        {

        }

    }


    /*
        Envia un mensaje al servidor
     */
    public int sendMessage(byte[] byteArray) throws IOException
    {
        try
        {
            if (this.socket != null && this.socket.isConnected() && this.outputStream != null)
            {
                //this.outputStreamWriter.wri .write(byteArray, offset, length);
                this.outputStream.write(byteArray);
                this.outputStream.flush();
                this.sendMessageFailureCounter = 0;
                return SEND_SUCCESS;
            }

            return SEND_ERROR;
        }
        catch (IOException exception)
        {
            throw exception;
        }
    }

    /*
        Inicia el proceso de escuchar mensajes
     */
    public void listenMessages() throws IOException
    {
        try
        {
            if (this.isClientConnected == true && !this.isBrokenPipe)
                this.messageReceived = this.bufferedReader.readLine();
            if (this.messageReceived != null)
            {
                String str = this.appName;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Message received: '");
                stringBuilder.append(this.messageReceived);
                stringBuilder.append("'");
                Log.d(str, stringBuilder.toString());

                this.listener.onReceiveTcpMessage(this.messageReceived);
            }
        }
        catch (Exception ex)
        {
            throw  ex;
        }
    }


    /*
        Cierra la conexion establecida con el servidor
     */
    public void closeConnection() throws IOException
    {
        try
        {
            if (this.socket != null)
                this.socket.close();

            if (this.bufferedReader != null)
                this.bufferedReader.close();

            if (this.bufferWriter != null)
                this.bufferWriter.close();

            this.isClientConnected = false;
        }
        catch (IOException exception)
        {
            throw  exception;
        }
    }


    /*
    Verifica si el cliente esta conectado o no
     */
    public boolean isClientConnected() { return this.isClientConnected; }


}

