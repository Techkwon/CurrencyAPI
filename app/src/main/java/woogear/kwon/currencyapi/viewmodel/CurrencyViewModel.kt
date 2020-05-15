package woogear.kwon.currencyapi.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import woogear.kwon.currencyapi.utils.APIClient
import woogear.kwon.currencyapi.utils.CurrencyService
import woogear.kwon.currencyapi.R
import woogear.kwon.currencyapi.model.CurrencyData
import woogear.kwon.currencyapi.utils.Constants.API_KEY
import woogear.kwon.currencyapi.utils.Constants.CURRENCIES
import woogear.kwon.currencyapi.utils.Constants.FORMAT

class CurrencyViewModel(application: Application): AndroidViewModel(application) {
    internal val currencyLiveData = MutableLiveData<CurrencyData>()
    internal val errorLiveData = MutableLiveData<String>()

    internal fun getCurrency() = viewModelScope.launch(Dispatchers.IO) {
        val api: CurrencyService = APIClient.getClient()
            .create(CurrencyService::class.java)
        val response = api.getCurrencies(API_KEY, CURRENCIES, FORMAT)
        if (response.isSuccessful){
            val result = response.body() as CurrencyData

            if (result.success) setCurrency(result)
            else setErrorMessage(getApplication<Application>().resources.getString(R.string.invalid_access_info))
        } else
            setErrorMessage(response.message())
    }

    private fun setCurrency(currency: CurrencyData) = viewModelScope.launch(Dispatchers.Main) {
        currencyLiveData.value = currency
    }

    private fun setErrorMessage(msg: String) = viewModelScope.launch(Dispatchers.Main) {
        errorLiveData.value = msg
    }
}