package com.metricsfab.ubimp.shared.models;

import com.google.android.gms.common.util.ArrayUtils;
import com.metricsfab.utils.types.PrimitiveDataUtils;

import java.util.Date;


public class LocationData {
    private boolean changed;

    private String imei;

    private double imeiDouble;

    private double latitude;

    private Date locationDateTime;

    private double longitude;

    private float speed;


    /**
     * Constructor de la clase
     * @param imei imei del dispositivo
     * @param latitude
     * @param longitude
     * @param speed
     * @param isDifferentFromThelastLocation Indica si la ubicacion es diferente comparada con la ubicacion anterior
     * @param locationDateTime
     */
    public LocationData(double imei, double latitude, double longitude, float speed, boolean isDifferentFromThelastLocation, Date locationDateTime) {
        this.imeiDouble         = imei;
        this.longitude          = longitude;
        this.latitude           = latitude;
        this.speed              = speed;
        this.locationDateTime   = locationDateTime;
        this.changed            = isDifferentFromThelastLocation;
    }

    
    public LocationData(String paramString, double paramDouble1, double paramDouble2, float paramFloat, boolean paramBoolean, Date paramDate) {
        this.imei = paramString;
        this.longitude = paramDouble1;
        this.latitude = paramDouble2;
        this.speed = paramFloat;
        this.locationDateTime = paramDate;
        this.changed = paramBoolean;
    }


    /**
     *
     * @param latitude Latitud
     * @param longitude Longitud
     * @param speed velocidad
     */
    public LocationData(double latitude, double longitude, float speed)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public boolean getChanged() { return this.changed; }

    public String getImei() { return this.imei; }

    public double getImeiDouble() { return this.imeiDouble; }

    public double getLatitude() { return this.latitude; }

    public Date getLocationDateTime() { return this.locationDateTime; }

    public double getLongitude() { return this.longitude; }

    public float getSpeed() { return this.speed; }


    /**
     * Esta funcion se encarga de dar formato a la informacion de ubicacion que se enviara a el servidor
     * El orden del formato es el siguiente imei latitud longitud velocidad
     * @return Los datos de ubicacion se convierten con el formato [ imei latitud longitud velocidad ]
     */
    public byte[] getLocationInByteArrayFormat()
    {
        return ArrayUtils.concatByteArrays(new byte[][] { PrimitiveDataUtils.ConvertDoubleToByteArray(this.imeiDouble), PrimitiveDataUtils.ConvertDoubleToByteArray(this.latitude), PrimitiveDataUtils.ConvertDoubleToByteArray(this.longitude), PrimitiveDataUtils.ConvertFloatToByteArray(this.speed)});
    }

    /**
     *
     * @return El imei como un arreglo de bytes
     */
    public byte[] getImeiInArrayFormat()
    {
       return PrimitiveDataUtils.ConvertDoubleToByteArray(this.imeiDouble);
    }
}
