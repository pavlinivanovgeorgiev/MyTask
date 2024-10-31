package com.homework.task.retrofit

import retrofit2.Call
import retrofit2.http.GET

interface CurrencyApiService {
    @GET("currency-exchange-rates/")
    fun getLatestRates(): Call<CurrencyResponse>
}