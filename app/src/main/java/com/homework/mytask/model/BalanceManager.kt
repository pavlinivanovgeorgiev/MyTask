import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class BalanceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)

    fun getBalance(currency: String): Double {
        return sharedPreferences.getFloat("balance_$currency", 0.0f).toDouble()
    }

    fun updateBalance(currency: String, amount: Double) {
        val currentBalance = getBalance(currency)
        val newBalance = currentBalance + amount
        if (newBalance < 0) {
            throw IllegalArgumentException("Balance cannot be negative.")
        }
        sharedPreferences.edit().putFloat("balance_$currency", newBalance.toFloat()).apply()
    }

    fun setInitialBalance(currency: String, amount: Double) {
        sharedPreferences.edit().putFloat("balance_$currency", amount.toFloat()).apply()
    }

    fun isInitialBalanceSet(currency: String): Boolean {
        return sharedPreferences.contains("balance_$currency")
    }
}