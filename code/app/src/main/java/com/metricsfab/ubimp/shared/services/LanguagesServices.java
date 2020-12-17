package com.metricsfab.ubimp.shared.services;


import com.metricsfab.ubimp.shared.dtos.LanguageDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LanguagesServices {

    /**
     * Obtiene los lenguajes disponibles en el servidor
     * @return
     */
    @GET("/languages")
    public Call<List<LanguageDto>> getLanguages();

}
