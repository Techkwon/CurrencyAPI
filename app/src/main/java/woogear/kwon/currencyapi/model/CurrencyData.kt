package woogear.kwon.currencyapi.model

data class CurrencyData(val success: Boolean,
                        val timeStamp: Long,
                        val source: String,
                        val quotes: HashMap<String, Float>) {

}