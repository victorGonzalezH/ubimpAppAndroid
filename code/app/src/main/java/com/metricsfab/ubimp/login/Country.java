package com.metricsfab.ubimp.login;

public class Country {


    /**
     * Nombre del pais
     */
    public String Name;


    /**
     * Codigo telfonico del pais
     */
    public String PhoneCode;


    /**
     * Identificador alternativo del pais
     */
    public  String CountryId;

    /**
     *
     * @param name
     * @param phoneCode
     */
    public  Country(String name, String phoneCode, String countryId)
    {
        this.Name = name;
        this.PhoneCode = phoneCode;
        this.CountryId = countryId;
    }

}
