package pl.rybson.currencyconverter.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.rybson.currencyconverter.models.ApiResponse
import pl.rybson.currencyconverter.retrofit.ApiService
import pl.rybson.currencyconverter.retrofit.ResourceData
import javax.inject.Inject

class MainRepository @Inject constructor(private val apiService: ApiService) {

    fun convertCurrency(from: String, to: String, amount: Double): Flow<ResourceData<ApiResponse>> = flow {
        emit(ResourceData.Loading)

        try {
            val currency = apiService.convertCurrency(from = from, to = to, amount = amount)
            emit(ResourceData.Success(currency))
        } catch (e: Exception) {
            emit(ResourceData.Error(e))
        }
    }

}
