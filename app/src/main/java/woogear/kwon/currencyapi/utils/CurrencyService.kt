package woogear.kwon.currencyapi.utils

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import woogear.kwon.currencyapi.model.CurrencyData

interface CurrencyService {
    @GET("/live")
    suspend fun getCurrencies(
        @Query("access_key") apiKey: String,
        @Query("currencies") currencies: String
    ): Response<CurrencyData>
}