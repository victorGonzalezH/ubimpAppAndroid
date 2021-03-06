package com.metricsfab.ubimp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.metricsfab.ubimp.main.MainActivity;
import com.metricsfab.ubimp.shared.UbimpServiceSettingsManager;
import com.metricsfab.ubimp.shared.models.LocationData;
import com.metricsfab.ubimpservice.R;
import com.metricsfab.utils.net.ITcpMessageListener;
import com.metricsfab.utils.net.TcpClient;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocationService extends LocationServiceBase implements MessengerListener, ITcpMessageListener, ConnectTcpTaskListener, SendTcpDataTaskListener
{

    public static final String CHANNEL_ID = "LocationServiceChannel";

    private static final boolean DEBUG = true;

    private static final String CLIENT_TYPE = "CLIENT_TYPE";

    /**
     * Operacion establecer el mensajero
     */
    public static final int OP_SET_MESSENGER = 1;

    /**
     * Operacion solicitar las ubicaciones
     */
    public static final int OP_REQUEST_LOCATION = 2;

    /**
     *
     */
    public static final int OP_NEW_LOCATION_ARRIVE = 3;


    /**
     * Indica la exactitud de la posicion del gps. El valor de esta variable se lo pasa el activity
     * que inicia el servicio, o lo obtiene de los datos guardados previamente
     */
    private int accuracy;

    /**
     * Nombre de la aplicacion, esta variable se usa en los mensajes toast o log info
     */
    private String appName;


    private int clientType;


    private boolean clientsBounded;

    /**
     * Es la tarea encargada de ejecutar el codigo de recepcion de datos del cliente TCP.
     */
    private ConnectTcpClientTask connectTcpClientTask;

    /**
     * Es la tarea encargada de ejecutar el codigo de envio de datos mediante el cliente TCP
     */
    private SendTcpDataTask sendTcpDataTask;

    /**
     * Indica la ultima ubicacion que se ha obtenido, por lo tanto se considera la ubicacion actual
     */
    private Location currentLocation;

    /**
     * Indica la ubicacion anterior a la actual. Esta ubicacion se actualiza siempre y cuando la ubicacion
     * actual sea diferente
     */
    private Location previousLocation;

    /**
     *
     */
    private long fastestUpdateInterval;


    /**
     * Proveedor de las ubicaciones. Este cliente se encarga de suministrar las ubicaciones del gps
     * de acuerdo a la documentacion, las ubicaciones pueden ser obtenidas en conjunto desde diferentes
     * sensores, el gps, la red celular, la red wifi etc. ademas de tomar en cuenta el estado de la
     * bateria, es decir brinda una localizacion fusionada (Fused)
     */
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Indica el nombre del servidor a el cual se conectada el client TCP
     */
    private String hostname;

    /**
     * Indica el codigo IMEI del celular
     */
    private double imeiDouble;

    private double latitude;

    private double latitudeAnt;

    /**
     * Indica el el cliente TCP se esta conectando
     */
    private boolean isTcpClientConnecting;


    public  boolean tcpClientConnecting()
    {
        return this.isTcpClientConnecting;
    }

    /**
     * Es la llamada que se hara cuando una nueva ubicacion llegue
     */
    private LocationCallback locationCallback;

    /**
     * Es la solicitud de ubicaciones
     */
    private LocationRequest locationRequest;

    /**
     * Es la cola de ubicaciones
     */
    private BlockingQueue<LocationData> locationsQueue;


    private double longitude;

    private double longitudeAnt;

    private boolean requestLocationUpdatesDone;

    private double speed;

    private double speedAnt;

    private TcpClient tcpClient;

    private int tcpPort;

    private int timeBetweenSendingTcpDataInMiliSeconds;

    private long updateInterval;

    /**
     * Indica si el usuario ha activado que le lleguen las actualizacion de ubicacion a la app
     */
    private boolean locationUpdatesRequestOfTheApp;


    /**
     * Mensajero que recibe los
     */
    private Messenger messengerToReceiveMessagesFromActivity;

    /**
     * Mensajero para enviar mensajes a la actividad
     */
    private Messenger messengerToSendMessagesToActivity;


    /**
     * Funciones///////////////////////////////////////////////////////////////////////////////////
     */
    /**
     * Crea la tarea para conectar el cliente tcp, este metodo no asegura que la conexion se realice con exito, para ello se debe de
     * // monitorear los eventos del objeto que implementa la interface SendTcpDataTaskListener
     * @param tcpClient Cliente tcp
     * @param debug Indica si se muestran o no los mensajes del debug
     */
    private void createConnectTcpClientTask(TcpClient tcpClient, boolean debug) {

        // Si la tarea es nulo entonces si instancia
        if (this.connectTcpClientTask == null)
        {
            this.connectTcpClientTask = new ConnectTcpClientTask(this);
            this.connectTcpClientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TcpClient[]{ tcpClient });
            if(debug)
            {
                Log.d(this.appName, "Launching connecting task, state was null");
            }
        } // Si la tarea es diferente de nulo y esta finalizada, entonces si instancia de nuevo para que vuelva a iniciar
        else if (this.connectTcpClientTask != null && this.connectTcpClientTask.getStatus() == AsyncTask.Status.FINISHED)
        {
            this.connectTcpClientTask = new ConnectTcpClientTask(this);
            this.connectTcpClientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TcpClient[]{ tcpClient });
            if(debug)
            {
                Log.d(this.appName, "Launching connecting task, state was finished");
            }

        }
        else if (this.connectTcpClientTask != null && this.connectTcpClientTask.getStatus() == AsyncTask.Status.PENDING)
        {
            this.connectTcpClientTask.cancel(true);
            this.connectTcpClientTask = new ConnectTcpClientTask(this);
            this.connectTcpClientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TcpClient[]{ tcpClient });
            if(debug)
            {
                Log.d(this.appName, "Launching connecting task, state was pending");
            }
        }
        else if(this.connectTcpClientTask != null && this.connectTcpClientTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            this.connectTcpClientTask.cancel(true);
            this.connectTcpClientTask = new ConnectTcpClientTask(this);
            this.connectTcpClientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TcpClient[]{ tcpClient });
            if(debug)
            {
                Log.d(this.appName, "Launching connecting task, state was pending");
            }
        }
    }


    /**
     * Crea un objeto LocationRequest, para la solicitud de ubicaciones
     * @param interval
     * @param fastestUpdateInterval
     * @param priority
     * @return
     */
    private LocationRequest createLocationRequest(long interval, long fastestUpdateInterval, int priority) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestUpdateInterval);
        locationRequest.setPriority(priority);
        return locationRequest;
    }


    /**
     * Indica si la posicion actual es igual a la posicion anterior
     * @return
     */
    private boolean locationValuesAreTheSame()
    { return (this.speed == this.speedAnt && this.latitude == this.latitudeAnt && this.longitude == this.longitudeAnt); }


    /**
     * Compara dos ubicaciones tomando en cuenta la latitud, longitud y la verlocidad
     * @param location1
     * @param location2
     * @return Verdadero sin son iguales, falso si son diferentes.
     */
    private boolean compareLocation(Location location1, Location location2)
    {
        if(location1 != null && location2 != null)
        {
            return location1.getAltitude() == location2.getAltitude() && location1.getLongitude() == location2.getLongitude()
                    && location1.getSpeed() == location2.getSpeed();
        }

        return false;
    }


    /**
     *
     * @param newLocation
     */

    /**
     * Agrega una ubicacion tipo Location a la cola global de ubicaciones tipo LocationData
     * @param newLocation Ubicacion que se usara para agregar la nueva ubicacion de tipo LocationData
     * @param debug Indica si se desea habilitar el debug para esta funcion e imprimir lo que suceda
     *              dentro de la funcion
     * @return
     */
    public boolean addLocationToTheQueue(Location newLocation, Date locationDate, boolean isDifferentFromTheLastLocation, boolean debug)
    {
        LocationData locationData = new LocationData(this.imeiDouble, newLocation.getLatitude(), newLocation.getLongitude(), newLocation.getSpeed(), isDifferentFromTheLastLocation, locationDate);
        boolean added = this.locationsQueue.offer(locationData);
        if (debug)
        {
            if(added)
                Log.d(this.appName, "Location added to the queue");
            else
                Log.d(this.appName, "Location no added to the queue");
        }

        return added;
    }


    /*
     * Establece la ubicacion global y la ubicacion anterior
     * @param newLocation
     * @return
     */
    private boolean setCurrentAndPreviousLocation(Location newLocation)
    {
        boolean equals = false;
        if (newLocation.getLongitude() != 0.0D && newLocation.getLatitude() != 0.0D)
        {
            this.currentLocation = newLocation;
            this.longitude      = this.currentLocation.getLongitude();
            this.latitude       = this.currentLocation.getLatitude();
            this.speed          = this.currentLocation.getSpeed();

             equals = compareLocation(this.currentLocation, this.previousLocation);

            // Si las ubicaciones son diferentes
            if(equals == false)
            {
                this.previousLocation = this.currentLocation;
            }
        }
        else
        {
            Log.d(this.appName, "Location null on newLocation");
        }

        return equals;
    }



    private void printLocationValues(Location newLocation)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Latitude: ");
        stringBuilder.append(this.currentLocation.getLatitude());
        Log.d(null, stringBuilder.toString());

        stringBuilder = new StringBuilder();
        stringBuilder.append("Longitude: ");
        stringBuilder.append(this.currentLocation.getLongitude());
        Log.d(null, stringBuilder.toString());

        stringBuilder = new StringBuilder();
        stringBuilder.append("Speed: ");
        stringBuilder.append(this.currentLocation.getSpeed());
        Log.d(null, stringBuilder.toString());

        /*
        context = getApplicationContext();
        stringBuilder = new StringBuilder();
        stringBuilder.append("Latitude: ");
        stringBuilder.append(this.currentLocation.getLatitude());
        Toast.makeText(context, stringBuilder.toString(), 0).show();
         */
    }

    /**
     * Establece
     * @param paramFusedLocationProviderClient
     */
    private void setToGetLastLocation(FusedLocationProviderClient paramFusedLocationProviderClient) {
        try {
            paramFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                public void onComplete(@NonNull Task<Location> locationTask) {

                    if (locationTask.isSuccessful() && locationTask.getResult() != null) {
                        LocationService.this.currentLocation = (Location)locationTask.getResult();
                        return;
                    }

                    Log.d(LocationService.this.appName, "Failed to get location");
                }
            });
            return;
        } catch (SecurityException exception) {
            String str = this.appName;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Lost location permission.");
            stringBuilder.append(paramFusedLocationProviderClient);
            Log.e(str, stringBuilder.toString());
        }
    }

    public LocationCallback getLocationCallback() { return this.locationCallback; }

    public LocationRequest getLocationRequest() { return this.locationRequest; }


    /**
     * Inicia la tarea de envio de datos mediante el cliente TCP. Itera sobre la cola global de ubicaciones hasta
     * enviar todas las ubicaciones, cuando las termina de enviar todas, la tarea finaliza. Para reactivar el envio, se debe
     * de llamar este metodo nuevamente
     */
    public void startSendTask(boolean debug) {

        // Si la tarea es nulo o si es diferente de nulo y su estatus esta en finalizado
        if (this.sendTcpDataTask == null || (this.sendTcpDataTask != null && this.sendTcpDataTask.getStatus() == AsyncTask.Status.FINISHED))
        {
            if(debug)
            {
                Log.d(this.appName, "Creating sending tcp data task");
            }

            // Instanciacion de la tarea
            this.sendTcpDataTask = new SendTcpDataTask(this);

            // Se ejecuta la tarea
            this.sendTcpDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.timeBetweenSendingTcpDataInMiliSeconds);
        }
        /*
        else if(this.sendTcpDataTask != null && this.sendTcpDataTask.getStatus() == AsyncTask.Status.RUNNING && locationsQueue.size() > 0)
        {
            this.sendTcpDataTask.cancel(true);
            boolean isCancelled = sendTcpDataTask.isCancelled();
            this.sendTcpDataTask = new SendTcpDataTask(this);
            this.sendTcpDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.timeBetweenSendingTcpDataInMiliSeconds);
        }
        */
    }


    public boolean isRequestLocationUpdatesDone() { return this.requestLocationUpdatesDone; }



    @Override
    public void setConnectingFlag(boolean connecting) {
        this.isTcpClientConnecting = connecting;
    }

    /**
     * Evento que se dispara cuand se envia una ubicacion
     * @param bytesSent Numero de bytes enviados
     */
    @Override
    public void onSendTcpDataProgressUpdate(int bytesSent) {

    }

    @Override
    public void onSendTcpDataPostExecute(int totalBytesSent) {
        if(this.sendTcpDataTask.getStatus() == AsyncTask.Status.FINISHED)
        {
            Toast.makeText(getApplicationContext(), "Send task finished", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String getApplicationName() {
        return this.appName;
    }

    @Override
    public BlockingQueue<LocationData> getLocationQueue() {
        return this.locationsQueue;
    }

    @Override
    public TcpClient getTcpClient() {
        return this.tcpClient;
    }

    @Override
    public void onTcpClientDisconnected() {

    }

    @Override
    public void onDoInBackground() {

    }

    /**
     * Evento que se dispara cuando se termina la tarea de conexion del cliente tcp
     * @param clientConnected Indica si el cliente se ha conectado o no
     */
    @Override
    public void onConnectTcpClientTaskResult(boolean clientConnected)
    {
        // Si el cliente tcp se conecto exitosamente, entonces si inicia la tarea para enviar los datos
        // de la cola de ubicaciones
        if (clientConnected == true)
        {
            this.startSendTask(DEBUG);

        }
        else // Si no se conecto el cliente tcp, se vuelve a intentar
        {
            createConnectTcpClientTask(this.tcpClient, DEBUG);
        }

    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Location Service Channel",  NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Eventos/////////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * Evento onCreate, que se dispara al crearse el servicio
     */
    @Override
    public void onCreate() {
        // Toast
        Toast.makeText(getApplicationContext(), "Creating ubimp service", Toast.LENGTH_LONG).show();

        this.requestLocationUpdatesDone = false;

        // Se instancia el proveedor de las ubicaciones
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Se instancia la llamada de vuelta de la ubicaciones, esta se ejecuta cada vez que se obtiene
        // una ubicacion nueva.
        this.locationCallback = new LocationCallback() {

            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);

                // Establece las ubicaciones actuales y anterior
                boolean locationsAreEquals = setCurrentAndPreviousLocation(locationResult.getLastLocation());

                // Agrega la nueva ubicacion a la cola de ubicaciones
                Location lastLocation = locationResult.getLastLocation();
                addLocationToTheQueue(lastLocation, new Date(), locationsAreEquals, true);


                // Si el cliente tcp no se esta conectando, se inicia la tarea para enviar las ubicaciones
                if (tcpClientConnecting() == false && tcpClient.isClientConnected() == true)
                {
                    startSendTask(DEBUG);
                }

                LocationData locationData = new LocationData(lastLocation.getLatitude(), lastLocation.getLongitude(),lastLocation.getSpeed());
                Message msg = Message.obtain(null, OP_NEW_LOCATION_ARRIVE, locationData);
                try
                {
                    messengerToSendMessagesToActivity.send(msg);

                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }


            }
        };

        // Instancia la cola de las ubicaciones
        this.locationsQueue = new LinkedBlockingQueue();

        setToGetLastLocation(this.fusedLocationClient);
    }

    /**
     * Evento que se ejecuta cuando se destruye el servicio
     */
    @Override
    public void onDestroy()
    {
        LocationService.ServiceStarted = false;
        if(DEBUG)
        {
            Log.d(this.appName, "On Destroy Service");
        }

        if (this.tcpClient != null)
        {
            try
            {
                this.tcpClient.closeConnection();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

    }

    /**
     *
     * @param paramIntent
     */
    @Override
    public void onRebind(Intent paramIntent) {
        Log.i(this.appName, "in onRebind()");
        this.clientsBounded = true;
        super.onRebind(paramIntent);
    }


    @Override
    public IBinder onBind(Intent paramIntent)
    {
        Log.i(this.appName, "Cliente conectado");
        this.clientsBounded = true;

        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();

        this.messengerToReceiveMessagesFromActivity = new Messenger(new LocationServiceHandler(this, this));
        return messengerToReceiveMessagesFromActivity.getBinder();

    }

    public void onReceiveTcpMessage(String paramString) {}


    /**
     * Evento que se dispara al iniciar el servicio
     * @param intent Objeto Intento
     * @param flags banderas acerca de la solicitud de inicio
     * @param startId identificador unico que identifica la solicitud del intento de inicio del servicio
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.d("Service", "on Start");

        // Se activa la bandera estatica de la clase LocationService para indicar que el servicio se ha iniciado
        LocationService.ServiceStarted = true;

        // Si el intento es diferente de null, entonces se obtiene los datos para configurar el cliente
        // tcp y obtener los valores de las de mas variables
        if (intent != null)
        {
            this.appName                = intent.getStringExtra(UbimpServiceSettingsManager.APP_NAME_LABEL);
            this.updateInterval         = intent.getLongExtra(UbimpServiceSettingsManager.UPDATE_INTERVAL_LABEL, 10000L);
            this.fastestUpdateInterval  = intent.getLongExtra(UbimpServiceSettingsManager.FASTEST_UPDATE_INTERVAL_LABEL, 5000L);
            this.accuracy               = intent.getIntExtra(UbimpServiceSettingsManager.ACCURACY_LABEL, 100);
            this.hostname               = intent.getStringExtra(UbimpServiceSettingsManager.TCP_HOSTNAME_LABEL);
            this.tcpPort                = intent.getIntExtra(UbimpServiceSettingsManager.TCP_PORT_LABEL, 49371);
            this.imeiDouble             = intent.getDoubleExtra(UbimpServiceSettingsManager.IMEI_DOUBLE_LABEL, 0.0D);
            this.locationUpdatesRequestOfTheApp = intent.getBooleanExtra(UbimpServiceSettingsManager.LOCATION_UPDATES_REQUEST_OF_THE_APP_LABEL, false);
            if (this.locationRequest == null)
            {
                this.locationRequest = createLocationRequest(this.updateInterval, this.fastestUpdateInterval, this.accuracy);
                Toast.makeText(getApplicationContext(), "Starting ubimp service", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            UbimpServiceSettingsManager ubimpServiceSettingsManager = new UbimpServiceSettingsManager(getApplicationContext());
            this.appName = "Ubimp service";
            this.hostname = ubimpServiceSettingsManager.getHostNameFromSettings("www.ubimp.com");
            this.tcpPort = ubimpServiceSettingsManager.getTcpPortFromSettings(49371);
            this.imeiDouble = ubimpServiceSettingsManager.getImeiDouble();
            if (this.locationRequest == null)
            {
                this.updateInterval = ubimpServiceSettingsManager.getUpdateInterval(10000L);
                this.fastestUpdateInterval = ubimpServiceSettingsManager.getFastestUpdateInterval(5000L);
                this.accuracy = ubimpServiceSettingsManager.getAccuracy(100);
                this.locationRequest = createLocationRequest(this.updateInterval, this.fastestUpdateInterval, this.accuracy);
            }

            this.locationUpdatesRequestOfTheApp = ubimpServiceSettingsManager.isLocationUpdateRequestOfTheAppEnabled();
            Toast.makeText(getApplicationContext(), "Starting ubimp service", Toast.LENGTH_LONG).show();
        }

        // SOLO PARA FINES DE PRUEBAS <--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        this.imeiDouble = 1234567890;


        // Solicita las actualizaciones de las ubicaciones
        if(this.locationUpdatesRequestOfTheApp)
        {
            requestLocationUpdates(this.locationRequest, this.locationCallback);
        }

        // Si el cliente tcp es nulo, se procede a instanciarlo
        if (this.tcpClient == null)
        {
            this.tcpClient = new TcpClient(this.hostname, this.tcpPort, this, this.appName);
        }

        // Se inicia la tarea para conectar a el cliente tcp
        createConnectTcpClientTask(this.tcpClient, true);


        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(this.appName)
                .setContentText("Ubimp service running")
                .setSmallIcon(R.drawable.ic_dot)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_STICKY;
    }


    /**
     *
     * @param paramIntent
     * @return
     */
    @Override
    public boolean onUnbind(Intent paramIntent) {
        Log.i(this.appName, "On unbind");
        this.clientsBounded = false;
        return true;
    }


    /**
     * Solicita obtener las ubicaciones
     * @param paramLocationRequest Solicitud de ubicaciones
     * @param paramLocationCallback Llamada cuando se obtenga una nueva ubicacion en caso de que se configure correctamente
     */
    public void requestLocationUpdates(LocationRequest paramLocationRequest, LocationCallback paramLocationCallback) {
        Log.i(this.appName, "Requesting location updates");
        try
        {
            this.fusedLocationClient.requestLocationUpdates(paramLocationRequest, paramLocationCallback, Looper.myLooper()).addOnCompleteListener(new OnCompleteListener()
            {

                public void onComplete(@NonNull Task requestTask)
                {
                    if (requestTask.isSuccessful())
                    {
                        requestLocationUpdatesDone = true;
                        Toast.makeText(getApplicationContext(), "Ubimp: Requesting location updates success", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Log.d(appName, requestTask.getException().getMessage());
                        requestLocationUpdatesDone = false;
                        Context context = getApplicationContext();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Error requesting location updates");
                        stringBuilder.append(requestTask.getException().getMessage());
                        Toast.makeText(context, stringBuilder.toString(), Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
        catch (SecurityException paramLocationRequestException)
        {
            String str = this.appName;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Lost location permission. Could not request updates. ");
            stringBuilder.append(paramLocationRequest);
            Log.e(str, stringBuilder.toString());
        }
    }



    @Override
    public void forwardMessage(Message message) {

        switch (message.what)
        {
            // Operacion que establece el mensajero para responder a la actividad
            case OP_SET_MESSENGER:
                this.messengerToSendMessagesToActivity = message.replyTo;
                break;

            case OP_REQUEST_LOCATION:
                this.requestLocationUpdates(this.locationRequest, this.locationCallback);
                break;
        }

    }


    /**
     * Clase tarea encargada de conectar el cliente tcp
     */
    public class ConnectTcpClientTask extends AsyncTask<TcpClient, Integer, Boolean>
    {
        // Escuchador para la tarea
        private  ConnectTcpTaskListener connectTcpTaskListener;

        private TcpClient tcpClient;

        /**
         * Inicia la tarea
         * @param connectTcpTaskListener
         */
        public ConnectTcpClientTask(ConnectTcpTaskListener connectTcpTaskListener)
        {
            this.connectTcpTaskListener = connectTcpTaskListener;
        }


        @Override
        protected Boolean doInBackground(TcpClient... tcpClients)
        {
            try
            {
                tcpClient = tcpClients[0];

                // Se establece
                this.connectTcpTaskListener.setConnectingFlag(true);

                // Si el cliente no esta conectado y la conexion resulta exitosa
                if (!this.tcpClient.isClientConnected() && this.tcpClient.connect() == 0)
                {
                    Log.d(this.connectTcpTaskListener.getApplicationName(), "Tcp client connected");
                    this.connectTcpTaskListener.setConnectingFlag(false);
                    // Si se desea mostrar el progreso de la operacion
                    // publishProgress();
                }
                else
                {
                    Log.d(this.connectTcpTaskListener.getApplicationName(), "Tcp client can not connect");
                }
            }
            catch (IOException exception)
            {
                Log.e(this.connectTcpTaskListener.getApplicationName(), exception.getMessage());
            }

            Log.e(this.connectTcpTaskListener.getApplicationName(), "Saliendo de tratando de conectar");

            return Boolean.valueOf(this.tcpClient.isClientConnected());
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            this.connectTcpTaskListener.setConnectingFlag(false);

            String str = this.connectTcpTaskListener.getApplicationName();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Client connected: ");
            stringBuilder.append(result);
            Log.e(str, stringBuilder.toString());

            this.connectTcpTaskListener.onConnectTcpClientTaskResult(result.booleanValue());
        }

        @Override
        protected void onProgressUpdate(Integer... progressUpdate) { super.onProgressUpdate(progressUpdate); }
    }


    /**
     *
     */
    public class SendTcpDataTask extends AsyncTask<Integer, Integer, Integer> {


        private SendTcpDataTaskListener sendTcpDataTaskListener;
        int timeBetweenSendingTcpDataInMiliSeconds = 100;

        public SendTcpDataTask(SendTcpDataTaskListener sendTcpDataTaskListener) {
            this.sendTcpDataTaskListener = sendTcpDataTaskListener;
        }

        @Override
        protected Integer doInBackground(Integer... timesBetweenSendingTcpDataInMiliSeconds)
        {

            int totalBytesSent = 0;

            // Se obtiene el tiempo entre cada envio de datos
            this.timeBetweenSendingTcpDataInMiliSeconds = timesBetweenSendingTcpDataInMiliSeconds[0];

            // Indica el numero de bytes que se enviaron
            int numberBytesSent;

            // mientras la cola no esta vacia entonces, se procede a enviar las ubicaciones que
            // estan en la cola
            while (!this.sendTcpDataTaskListener.getLocationQueue().isEmpty())
            {
                numberBytesSent = 0;
                LocationData locationData = this.sendTcpDataTaskListener.getLocationQueue().peek();

                if (locationData != null)
                {
                    Log.d(this.sendTcpDataTaskListener.getApplicationName(), "Sending location");
                    try
                    {
                        byte[] locationBytes;
                        //if (locationData.getChanged())
                        {
                            locationBytes = locationData.getLocationInByteArrayFormat();
                        }
                        //else
                        //{
                          //  locationBytes = locationData.getImeiInArrayFormat();
                        //}

                        numberBytesSent = this.sendTcpDataTaskListener.getTcpClient().sendMessage(locationBytes);

                    } catch (IOException e)
                    {
                        e.printStackTrace();
                        numberBytesSent = 0;
                    }

                    // Si el numero de bytes enviados es mayor que cero entonces no hubo error y
                    // es seguro remover la ubicacion de la cola de ubicaciones
                    if (numberBytesSent > 0)
                    {
                        // Se remueve la ubicacion de la cola de ubicaciones
                        locationData = this.sendTcpDataTaskListener.getLocationQueue().poll();

                        try
                        {
                            Thread.sleep(this.timeBetweenSendingTcpDataInMiliSeconds);

                        } catch (InterruptedException exception)
                        {
                            Log.d(sendTcpDataTaskListener.getApplicationName(), exception.getMessage());
                        }

                        totalBytesSent += numberBytesSent;
                    }
                    else if (numberBytesSent == 0) // Si el numero de bytes es cero, probablemente hubo un error
                    {
                        // Se pregunta si el cliente tcp esta conectado, puede ser que por eso
                        //se haya provocado el error de envio
                        if (!this.sendTcpDataTaskListener.getTcpClient().isClientConnected())
                        {
                            // Si desconecto el cliente tcp, se notifica a el componente quien mando a llamar a la tarea
                            this.sendTcpDataTaskListener.onTcpClientDisconnected();
                        }
                    }

                }


                if (isCancelled())
                    return totalBytesSent;
            }

            return totalBytesSent;
        }

        @Override
        protected void onPostExecute(Integer totalBytesSent)
        {
            super.onPostExecute(totalBytesSent);
            this.sendTcpDataTaskListener.onSendTcpDataPostExecute(totalBytesSent);
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            super.onProgressUpdate(progress);
            this.sendTcpDataTaskListener.onSendTcpDataProgressUpdate(progress[0]);
        }
    }


    /**
     * Manejador de mensajes que se reciben
     */
    static class LocationServiceHandler extends Handler
    {
        private Context applicationContext;

        private MessengerListener listener;

        LocationServiceHandler(Context context, MessengerListener listener) {
            applicationContext = context.getApplicationContext();
            this.listener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {

                default:
                    super.handleMessage(msg);
                    listener.forwardMessage(msg);
            }
        }
    }

}

