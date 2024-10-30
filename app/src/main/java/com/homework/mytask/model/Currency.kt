class Currency(val name: String, val rateToEuro: Double) {

    companion object {
        private val currencyMap = mutableMapOf<String, Currency>()

        fun addCurrency(name: String, rateToEuro: Double) {
            currencyMap[name] = Currency(name, rateToEuro)
        }

        fun getAllCurrencies(): List<Currency> = currencyMap.values.toList()

        fun fromName(name: String): Currency? {
            return currencyMap[name]
        }
    }

    override fun toString(): String {
        return "$name (rate to EUR: $rateToEuro)"
    }
}