@file:Suppress("DEPRECATION")
package woogear.kwon.currencyapi.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import woogear.kwon.currencyapi.R
import woogear.kwon.currencyapi.model.CurrencyData

object CommonUtils {
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun isNetworkAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // VERSION_CODES.M = API 23
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            capabilities ?: return false

            result = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }

        return result
    }

    fun setDataArrays(currencies: Array<String?>, countries: Array<String?>, data: CurrencyData, Activity: Activity) {
        var i = 0

        for (quote in data.quotes) {
            currencies[i] = quote.key.substring(3) // ex) USDKRW -> KRW
            countries[i] = when (currencies[i]) {
                Exchange.KRW.name -> Activity.getString(R.string.currency_korea)
                Exchange.PHP.name -> Activity.getString(R.string.currency_philippines)
                Exchange.JPY.name -> Activity.getString(R.string.currency_japan)
                else -> Activity.getString(R.string.something_went_wrong)
            }
            i++
        }
    }
}