package com.metricsfab.ubimp.login;

public class ActivateDeviceCommand {


    /**
     *
     * @param imei
     * @param email
     * @param password
     * @param phoneNumber
     * @param countryId
     * @param timeStamp
     */
    public ActivateDeviceCommand(String imei, String email, String password, String phoneNumber, String countryId, String timeStamp, String lang)
    {
        this.Email = email;
        this.Password = password;
        this.PhoneNumber = phoneNumber;
        this.CountryId = countryId;
        this.TimeStamp = timeStamp;
        this.Imei = imei;
        this.Lang = lang;
    }

    /**
     * IMEI del dispositivo
     */
    public String Imei;

    /**
     * Correo
     */
    public String Email;

    /**
     * Contrasena
     */
    public String Password;

    /**
     * Numero de telefono
     */
    public String PhoneNumber;

    /**
     * Identificador alternativo del pais
     */
    public String CountryId;

    /**
     * Sello de tiempo
     */
    public String TimeStamp;


    public String Lang;
}
