package com.metricsfab.ubimp;


import android.content.Context;
import android.content.SharedPreferences;

public class UbimpServiceSettingsManager
{

    public static final String PLAY_SERVICES_PACKAGE_NAME = "com.google.android.gms";

    public static final int ACCURACY_DEFAULT = 100;

    public static final String ACCURACY_LABEL = "ACCURACY";

    public static final String APP_NAME = "UBIMP SERVICE";

    public static final String APP_NAME_LABEL = "APP_NAME";

    public static final String DEFAULT_HOSTNAME = "www.ubimp.com";

    public static final int DEFAULT_HTTPS_PORT = 443;

    public static final int DEFAULT_TCP_PORT = 49371;

    public static final long FASTEST_UPDATE_INTERVAL_DEFAULT = 5000L;

    public static final String FASTEST_UPDATE_INTERVAL_LABEL = "FASTEST_UPDATE_INTERVAL";

    public static final String HOSTNAME_LABEL = "HOSTNAME";

    public static final String HTTPS_PORT_LABEL = "HTTPS_PORT";

    public static final String IMEI_DOUBLE_LABEL = "IMEI_DOUBLE";

    public static final String LOCATION_SERVICE_TYPE_CLASS_LABEL = "LOCATION_SERVICE_TYPE_CLASS";

    public static final String LOCATION_UPDATES_REQUEST_OF_THE_APP_LABEL = "LOCATION_UPDATES_REQUEST_OF_THE_APP";

    public static final String PLAY_SERVICES_UPDATED_LABEL = "PLAY_SERVICES_UPDATED";

    public static final String REGISTERED_VALUE_LABEL = "REGISTERED_VALUE";

    public static final String TCP_PORT_LABEL = "TCP_PORT";

    public static final int TIME_BETWEEN_SENDING_TCP_DATA_IN_MILISECONDS_DEFAULT = 500;

    public static final String TIME_BETWEEN_SENDING_TCP_DATA_IN_MILISECONDS_LABEL = "TIME_BETWEEN_SENDING_TCP_DATA_IN_MILISECONDS";

    public static final String UBIMP_SERVICE_SETTINGS = "UBIMP_SERVICE_SETTINGS";

    public static final String UBIMP_SERVICE_SETTINGS_LABEL = "SERVICE_SETTINGS";

    public static final long UPDATE_INTERVAL_DEFAULT = 10000L;

    public static final String UPDATE_INTERVAL_LABEL = "UPDATE_INTERVAL";

    private Context context;

    private int mode = 0;

    private String settingsName = "UBIMP_SERVICE_SETTINGS";

    public UbimpServiceSettingsManager(Context paramContext)
    { this.context = paramContext; }

    public UbimpServiceSettingsManager(String paramString, int paramInt, Context paramContext) { this.context = paramContext; }

    public int getAccuracy(int paramInt) { return this.context.getSharedPreferences(this.settingsName, this.mode).getInt("ACCURACY", paramInt); }

    public String getAppName() { return "Ubimp Service"; }

    public SharedPreferences.Editor getEditor()
    { return this.context.getSharedPreferences(this.settingsName, this.mode).edit(); }

    public long getFastestUpdateInterval(long paramLong) { return this.context.getSharedPreferences(this.settingsName, this.mode).getLong("FASTEST_UPDATE_INTERVAL", paramLong); }

    public String getHostNameFromSettings(String paramString) { return this.context.getSharedPreferences(this.settingsName, this.mode).getString("HOSTNAME", paramString); }

    public double getImeiDouble() { return Double.longBitsToDouble(this.context.getSharedPreferences(this.settingsName, this.mode).getLong("IMEI_DOUBLE", Double.doubleToLongBits(0.0D))); }

    public int getTcpPortFromSettings(int paramInt) { return this.context.getSharedPreferences(this.settingsName, this.mode).getInt("TCP_PORT", paramInt); }

    public long getUpdateInterval(long paramLong) { return this.context.getSharedPreferences(this.settingsName, this.mode).getLong("UPDATE_INTERVAL", paramLong); }

    public boolean isDeviceRegistered() { return this.context.getSharedPreferences(this.settingsName, this.mode).getBoolean("REGISTERED_VALUE", false); }

    public boolean isLocationUpdateRequestOfTheAppEnabled() { return this.context.getSharedPreferences(this.settingsName, this.mode).getBoolean("LOCATION_UPDATES_REQUEST_OF_THE_APP", false); }
}
