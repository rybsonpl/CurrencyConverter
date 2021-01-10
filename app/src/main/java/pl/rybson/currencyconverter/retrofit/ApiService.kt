package pl.rybson.currencyconverter.retrofit

import pl.rybson.currencyconverter.models.ApiResponse
import pl.rybson.currencyconverter.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("convert")
    suspend fun convertCurrency(
        @Query("api_key") apiKey: String = Constants.API_KEY,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double
    ): ApiResponse

}