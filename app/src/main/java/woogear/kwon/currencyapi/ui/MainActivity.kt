package woogear.kwon.currencyapi.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_network_disconnected.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import woogear.kwon.currencyapi.utils.CommonUtils.isNetworkAvailable
import woogear.kwon.currencyapi.viewmodel.CurrencyViewModel
import woogear.kwon.currencyapi.R
import woogear.kwon.currencyapi.model.CurrencyData
import woogear.kwon.currencyapi.utils.Exchange
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var source = "USD"
    private var sourceCountry = ""
    private var selectedCurrency = "null"
    private var selectedRate: Float = 0F

    private lateinit var viewModel: CurrencyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNetworkAvailable(this)) {
            viewModel = ViewModelProviders.of(this).get(CurrencyViewModel::class.java)
            getCurrency()
            watchAmountChange()
        } else {
            showNetworkError(getString(R.string.please_check_network_connection))
        }
    }

    private fun getCurrency() {
        this.container_progress.visibility = View.VISIBLE
        viewModel.getCurrency()

        viewModel.currencyLiveData.observe(this, Observer { data ->
             initPicker(data)
            updateSourceCountry(data.source)
            updateTime(data.timeStamp)
        })

        viewModel.errorLiveData.observe(this, Observer { msg ->
            showNetworkError(msg)
        })
    }

    private fun updateSourceCountry(source: String) {
        this.source = source
        this.sourceCountry = when (source) {
            Exchange.USD.name -> getString(R.string.currency_united_states)
            else -> "Error"
        }

        this.tv_source_right.text = sourceCountry
        this.tv_amount_source.text = source
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateTime(milliSeconds: Long) {
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds // System.currentTimeMillis()
        this.tv_time_right.text = formatter.format(calendar.time)
    }

    private fun initPicker(data: CurrencyData) {
        val currencies = arrayOfNulls<String>(data.quotes.size)
        val countries = arrayOfNulls<String>(data.quotes.size)

        var i = 0
        for (quote in data.quotes) {
            currencies[i] = quote.key.substring(3) // ex) USDKRW -> KRW
            countries[i] = when (quote.key.substring(3)) {
                Exchange.KRW.name -> getString(R.string.currency_korea)
                Exchange.PHP.name -> getString(R.string.currency_philippines)
                Exchange.JPY.name -> getString(R.string.currency_japan)
                else -> getString(R.string.something_went_wrong)
            }
            i++
        }

        number_picker.minValue = 0
        number_picker.maxValue = countries.size - 1
        number_picker.displayedValues = countries

        // 수취국가 기본값 설정
        updateCurrencyInfo(countries[0], currencies[0], data.quotes[source+currencies[0]])

        this.container_progress.visibility = View.GONE

        number_picker.setOnValueChangedListener { _, _, newVal ->
            val rate = data.quotes[source+currencies[newVal]]
            updateCurrencyInfo(countries[newVal], currencies[newVal], rate)
        }
    }

    private fun updateCurrencyInfo(country: String?, currency: String?, rate: Float?) {
        val formatter = DecimalFormat("###,###.##")
        val formattedRate = formatter.format(rate.toString().replace(",", "").toFloat())

        this.tv_receipt_right.text = country
        this.tv_rate_right.text = "$formattedRate $currency / $source"

        selectedCurrency = currency ?: "unknown"
        selectedRate = rate ?: 0F

        val input = this.et_amount.text ?: ""
        if (input.isNotEmpty()) updateResult(input.toString().toFloat())
    }

    private fun watchAmountChange() {
        this.et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
                        updateResult(s.toString().toFloat())
                    } else {
                        updateResult(0F)
                    }
                }
            }
        })
    }

    private fun updateResult(amount: Float) {
        val formatter = DecimalFormat("###,###,###.##")
        val formattedAmount = formatter.format((amount * selectedRate).toString().replace(",", "").toFloat())
        this.tv_result.text = "수취금액은 $formattedAmount $selectedCurrency 입니다"
    }

    private fun showNetworkError(msg: String) {
        this.container_progress.visibility = View.GONE
        this.constraint_layout_main.visibility = View.GONE
        this.constraint_network_disconnected.visibility = View.VISIBLE
        this.tv_network_disconnected.text = msg
    }
}
