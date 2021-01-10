package pl.rybson.currencyconverter.retrofit

import java.lang.Exception

sealed class ResourceData<out T> {
    data class Success<T>(val data: T): ResourceData<T>()
    data class Error(val exception: Exception) : ResourceData<Nothing>()
    object Loading: ResourceData<Nothing>()
}
