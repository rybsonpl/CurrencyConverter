package pl.rybson.currencyconverter.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pl.rybson.currencyconverter.R
import pl.rybson.currencyconverter.databinding.ActivityMainBinding
import pl.rybson.currencyconverter.utils.hideKeyboard
import pl.rybson.currencyconverter.utils.isConnectionAvailable
import pl.rybson.currencyconverter.viewmodels.MainViewModel
import pl.rybson.currencyconverter.viewmodels.MainViewModel.ConvertEvent.*
import java.math.BigDecimal
import java.math.RoundingMode

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdownCurrenciesLists()
        selectCurrency()
        setupValueTextView()

        binding.btnConvert.setOnClickListener {
            if (isConnectionAvailable()) {
                hideKeyboard(binding.root)
                viewModel.convertCurrency()
            } else {
                Snackbar.make(binding.root, getString(R.string.no_internet), Snackbar.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.eventChannel.collectLatest { event ->
                when (event) {
                    ShowFromIsEmptyMessage -> Snackbar.make(binding.root, getString(R.string.pick_currency_from), Snackbar.LENGTH_SHORT).show()
                    ShowToIsEmptyMessage -> Snackbar.make(binding.root, getString(R.string.pick_currency_to), Snackbar.LENGTH_SHORT).show()
                    ShowValueIsEmptyMessage -> Snackbar.make(binding.root, getString(R.string.amount_cannot_be), Snackbar.LENGTH_SHORT).show()
                    Loading -> setupLoading()
                    is Success -> setupSuccess(event.data.rates[viewModel.toCurrency.value]?.rate_for_amount!!)
                    is Error -> setupError(event.exception)
                }
            }
        }
    }

    private fun setupDropdownCurrenciesLists() {
        val list = resources.getStringArray(R.array.currencies)
        val arrayAdapterFrom = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
        val arrayAdapterTo = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
        binding.apply {
            autoCompleteFrom.setAdapter(arrayAdapterFrom)
            autoCompleteTo.setAdapter(arrayAdapterTo)
        }
    }

    private fun selectCurrency() {
        binding.apply {
            autoCompleteFrom.setOnItemClickListener { _, view, _, _ ->
                viewModel.fromCurrency.value = (view as TextView).text.toString().take(3)
            }
            autoCompleteTo.setOnItemClickListener { _, view, _, _ ->
                viewModel.toCurrency.value = (view as TextView).text.toString().take(3)
            }
        }
    }

    private fun setupValueTextView() {
        binding.etValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.amount.value = if (s.isNullOrBlank()) 0.0 else s.toString().toDouble()
            }
        })
    }

    private fun setupLoading() {
        binding.apply {
            btnConvert.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupSuccess(amount: Double) {
        binding.apply {
            btnConvert.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            val value = BigDecimal(amount).setScale(2, RoundingMode.CEILING)
            etResult.setText(value.toString())
        }
    }

    private fun setupError(exception: Exception) {
        Log.d(TAG, exception.message.toString())
        binding.apply {
            btnConvert.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
        Snackbar.make(binding.root, getString(R.string.error), Snackbar.LENGTH_SHORT).show()
    }
}