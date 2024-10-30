interface CommissionStrategy {
    fun calculateCommission(amount: Double, transactionCount: Int, rateToEuro: Double): Double
}

class FirstFiveFreeCommission : CommissionStrategy {
    override fun calculateCommission(amount: Double, transactionCount: Int, rateToEuro: Double): Double {
        return if (transactionCount <= 5) {
            0.0
        } else {
            amount * 0.007
        }
    }
}

class NoCommissionAbove200Euro : CommissionStrategy {
    override fun calculateCommission(amount: Double, transactionCount: Int, rateToEuro: Double): Double {
        val amountInEuro = amount / rateToEuro

        return if (amountInEuro >= 200) {
            0.0
        } else {
            amount * 0.007
        }
    }
}

class CommissionCalculator(private var strategy: CommissionStrategy) {
    fun calculateCommission(amount: Double, transactionCount: Int, rateToEuro: Double): Double {
        return strategy.calculateCommission(amount, transactionCount, rateToEuro)
    }

    // Change commission strategy
    fun setStrategy(newStrategy: CommissionStrategy) {
        strategy = newStrategy
    }
}
