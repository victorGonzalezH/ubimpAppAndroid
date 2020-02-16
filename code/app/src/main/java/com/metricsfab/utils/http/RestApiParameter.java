package com.metricsfab.utils.http;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

public class RestApiParameter
{
    public static final int DELETE = 4;

    public static final int GET = 1;

    public static final int POST = 2;

    public static final int PUT = 3;

    public String hostname;

    public int method;

    public Map<String, String> parameters;

    public int port;

    public String protocol;

    public String url;


    public RestApiParameter(String hostname, int port, String protocol, String url, int method)
    {
        this.hostname   = hostname;
        this.port       = port;
        this.protocol   = protocol;
        this.url        = url;
        this.method     = method;
        this.parameters = new HashMap();
    }


    /*
    Agrega un valor a los parametros
     */
    public void AddParameter(String key, String value)
    {
        this.parameters.put(key, value);
    }


    @Retention(RetentionPolicy.SOURCE)
    public static @interface HTTP_METHODS
    {

    }
}
