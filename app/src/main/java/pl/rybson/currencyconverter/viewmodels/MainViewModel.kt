package pl.rybson.currencyconverter.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.rybson.currencyconverter.models.ApiResponse
import pl.rybson.currencyconverter.repositories.MainRepository
import pl.rybson.currencyconverter.retrofit.ResourceData

class MainViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    val fromCurrency = MutableStateFlow("")
    val toCurrency = MutableStateFlow("")
    val amount = MutableStateFlow(0.0)

    private val convertEventChannel = Channel<ConvertEvent>()
    val eventChannel = convertEventChannel.receiveAsFlow()


    fun convertCurrency() = viewModelScope.launch {
        if (fromCurrency.value.isNotBlank()) {
            if (toCurrency.value.isNotBlank()) {
                if (amount.value != 0.0) {
                    sendRequest()
                } else {
                    convertEventChannel.send(ConvertEvent.ShowValueIsEmptyMessage)
                }
            } else {
                convertEventChannel.send(ConvertEvent.ShowToIsEmptyMessage)
            }
        } else {
            convertEventChannel.send(ConvertEvent.ShowFromIsEmptyMessage)
        }
    }

    private fun sendRequest() {
        viewModelScope.launch {
            repository.convertCurrency(fromCurrency.value, toCurrency.value, amount.value).collect { response ->
                when (response) {
                    ResourceData.Loading -> convertEventChannel.send(ConvertEvent.Loading)
                    is ResourceData.Success -> convertEventChannel.send(ConvertEvent.Success(response.data))
                    is ResourceData.Error -> convertEventChannel.send(ConvertEvent.Error(response.exception))
                }
            }
        }
    }

    sealed class ConvertEvent {
        object ShowFromIsEmptyMessage : ConvertEvent()
        object ShowToIsEmptyMessage : ConvertEvent()
        object ShowValueIsEmptyMessage : ConvertEvent()
        object Loading : ConvertEvent()
        data class Success(val data: ApiResponse) : ConvertEvent()
        data class Error(val exception: Exception) : ConvertEvent()
    }

}

