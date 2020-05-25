package woogear.kwon.currencyapi.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_network_disconnected.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import woogear.kwon.currencyapi.utils.CommonUtils.isNetworkAvailable
import woogear.kwon.currencyapi.viewmodel.CurrencyViewModel
import woogear.kwon.currencyapi.R
import woogear.kwon.currencyapi.model.CurrencyData
import woogear.kwon.currencyapi.utils.CommonUtils.setDataArrays
import woogear.kwon.currencyapi.utils.Exchange
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var source = "USD"
    private var sourceCountry = ""
    private var selectedCurrency = "null"
    private var selectedRate: Double = 0.00
    private lateinit var viewModel: CurrencyViewModel

    companion object {
        private const val INPUT_LIMIT = 10000.00
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isNetworkAvailable(this)) {
            viewModel = ViewModelProviders.of(this).get(CurrencyViewModel::class.java)
            getCurrency()
            watchAmountChange()
        } else
            showNetworkError(getString(R.string.please_check_network_connection))
    }

    private fun getCurrency() {
        this.container_progress.visibility = View.VISIBLE
        viewModel.getCurrency()

        viewModel.currencyLiveData.observe(this, Observer { data ->
            initPicker(data)
            updateSourceCountry(data.source)
            updateTime(data.timeStamp) // not working: only retrieves 1970-01-01 09:00 AM
            // updateTime(System.currentTimeMillis())
            this.container_progress.visibility = View.GONE
        })

        viewModel.errorLiveData.observe(this, Observer { msg ->
            showNetworkError(msg)
        })
    }

    private fun updateSourceCountry(source: String) {
        this.source = source
        this.sourceCountry = when (source) {
            Exchange.USD.name -> getString(R.string.currency_united_states)
            else -> "Unknown Country"
        }

        this.tv_source_right.text = sourceCountry
        this.tv_amount_source.text = source
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateTime(milliSeconds: Long) {
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm a")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds // System.currentTimeMillis()
        this.tv_time_right.text = formatter.format(calendar.time)
    }

    private fun initPicker(data: CurrencyData) {
        val currencies = arrayOfNulls<String>(data.quotes.size) // ex) KRW
        val countries = arrayOfNulls<String>(data.quotes.size) // ex) 한국(KRW)

        setDataArrays(currencies, countries, data, this)

        this.number_picker.minValue = 0
        this.number_picker.maxValue = countries.size - 1
        this.number_picker.displayedValues = countries

        // 수취국가 초기값 설정
        updateCurrencyInfo(countries[0], currencies[0], data.quotes[source+currencies[0]])

        this.number_picker.setOnValueChangedListener { _, _, newVal ->
            val rate = data.quotes[source + currencies[newVal]]
            updateCurrencyInfo(countries[newVal], currencies[newVal], rate)
        }
    }

    private fun updateCurrencyInfo(country: String?, currency: String?, rate: Double?) {
        val formattedRate = String.format("%,.2f", rate)

        this.tv_receipt_right.text = country
        this.tv_rate_right.text = String.format(getString(R.string.updated_currency_info), formattedRate, currency, source)

        selectedCurrency = currency ?: "unknown"
        selectedRate = rate ?: 0.00

        val input = this.et_amount.text ?: ""
        if (input.isNotEmpty()) updateResult(input.toString().toDouble())
    }

    private fun watchAmountChange() {
        this.et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
                        if (s.toString().toDouble() <= INPUT_LIMIT) updateResult(s.toString().toDouble())
                        else showInputAlarm()
                    } else {
                        updateResult(0.00)
                    }
                }
            }
        })
    }

    private fun updateResult(amount: Double) {
        val formattedAmount = String.format("%,.2f", amount * selectedRate)
        this.tv_result.text = String.format(getString(R.string.receipt_amount_notice), formattedAmount, selectedCurrency)
        this.tv_result.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
    }

    private fun showInputAlarm() {
        this.tv_result.text = getString(R.string.input_limit_alarm)
        this.tv_result.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
    }

    private fun showNetworkError(msg: String) {
        this.container_progress.visibility = View.GONE
        this.constraint_layout_main.visibility = View.GONE
        this.constraint_network_disconnected.visibility = View.VISIBLE
        this.tv_network_disconnected.text = msg
    }
}
