import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.homework.mytask.R
import com.homework.task.retrofit.CurrencyResponse
import com.homework.task.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {
    private val balanceManager = BalanceManager(application)
    private var commissionCalculator = CommissionCalculator(FirstFiveFreeCommission())
    val exchangeRates = MutableLiveData<Map<String, Double>>()
    val balance = MutableLiveData<Map<String, Double>>()
    val errorMessage = MutableLiveData<String>()
    val convertedValue = MutableLiveData<String>()
    val receiveAmount = MutableLiveData<Double>()
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
    private var transactionCount: Int
        get() = sharedPreferences.getInt("transaction_count", 0)
        set(value) = sharedPreferences.edit().putInt("transaction_count", value).apply()
    var isInitial = true

    init {
        // Start periodic update of currency rates every 5 seconds
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                fetchCurrencyRates()
                delay(5000)
                isInitial = false
            }
        }

        // add currency manually
//        Currency.addCurrency("GBP", 0.85)

        // Set initial balance on first launch
        if (!balanceManager.isInitialBalanceSet("EUR")) {
            balanceManager.setInitialBalance("EUR", 1000.0)
            balance.value = getPositiveBalances()
        }
    }

    // Load currency rates from the network
    fun fetchCurrencyRates() {
        RetrofitInstance.api.getLatestRates().enqueue(object : Callback<CurrencyResponse> {
            override fun onResponse(
                call: Call<CurrencyResponse>,
                response: Response<CurrencyResponse>
            ) {

                if (response.isSuccessful && response.body() != null) {
                    val rates = response.body()?.rates

                    rates?.forEach { (currencyName, rateToEuro) ->
                        Currency.addCurrency(currencyName, rateToEuro)
                    }

                    // update spinners and balance list only on initial
                    if (isInitial) {
                        exchangeRates.postValue(rates!!)
                        balance.value = getPositiveBalances()
                    }
                } else {
                    errorMessage.postValue(
                        Resources.getSystem().getString(R.string.failed_to_load_data)
                    )
                }
            }

            override fun onFailure(call: Call<CurrencyResponse>, t: Throwable) {
                errorMessage.postValue(
                    Resources.getSystem().getString(R.string.error_loading_data, { t.message })
                )
            }
        })
    }

    // Convert currency
    fun convertCurrency(amount: Double, fromCurrency: Currency, toCurrency: Currency) {
        commissionCalculator.setStrategy(NoCommissionAbove200Euro())
        val commission = commissionCalculator.calculateCommission(
            amount,
            transactionCount,
            fromCurrency.rateToEuro
        )
        val totalCost = amount + commission
        // Check if the balance is sufficient and perform conversion
        balanceManager.updateBalance(fromCurrency.name, -totalCost)
        val amountInEuro = amount * toCurrency.rateToEuro
        val convertedAmount = amountInEuro / fromCurrency.rateToEuro
        balanceManager.updateBalance(toCurrency.name, convertedAmount)
        transactionCount++
        balance.value = getPositiveBalances()
        convertedValue.postValue("You have converted $amount ${fromCurrency.name} to $convertedAmount ${toCurrency.name}. Commission Fee - $commission ${fromCurrency.name}.")
    }

    // Get all balances
    private fun getAllBalances(): Map<String, Double> {
        val currencies = Currency.getAllCurrencies()

        val balances = mutableMapOf<String, Double>()
        currencies.forEach { currency ->
            balances[currency.name] = balanceManager.getBalance(currency.name)
        }
        return balances
    }

    // Get balances only for currencies with positive balance
    fun getPositiveBalances(): Map<String, Double> {
        return getAllBalances().filter { it.value > 0 }
    }

    // Real-time calculation for receive amount
    fun calculateReceiveAmount(amount: Double, fromCurrency: Currency, toCurrency: Currency) {
        val amountInEuro = amount * toCurrency.rateToEuro
        val convertedAmount = amountInEuro / fromCurrency.rateToEuro
        receiveAmount.postValue(convertedAmount)
    }
}
