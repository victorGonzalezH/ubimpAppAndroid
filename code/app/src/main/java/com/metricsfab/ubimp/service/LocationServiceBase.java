package com.metricsfab.ubimp.service;

import android.app.Service;

public abstract class LocationServiceBase extends Service {

    public static final int CLIENT_BOOT_BROADCAST_RECEIVER = 2;

    public static final int ClientActivity = 1;

    public static boolean ServiceStarted = false;

    public static final int V1 = 1;

    public static final int V2 = 2;

    public static final int V3 = 3;
}
