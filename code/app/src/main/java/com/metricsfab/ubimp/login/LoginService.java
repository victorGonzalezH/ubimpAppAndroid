package com.metricsfab.ubimp.login;

import com.metricsfab.utils.http.ApiResultBase;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Servicio del login
 */
public interface LoginService {

    /**
     * Obtiene el listado de paises
     * @return Listado de paises
     */
    @GET("/devices/countries")
    public Call<ApiResultBase> getCountries();


    @POST("devices/activate")
    Call<ApiResultBase> activateDevice(@Body ActivateDeviceCommand activateDeviceCommand);

}
