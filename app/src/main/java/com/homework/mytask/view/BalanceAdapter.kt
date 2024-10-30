package com.homework.mytask.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homework.mytask.R

class BalanceAdapter : RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder>() {

    private var balances: Map<String, Double> = emptyMap()

    fun updateBalances(newBalances: Map<String, Double>) {
        balances = newBalances
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_balance, parent, false)
        return BalanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: BalanceViewHolder, position: Int) {
        val currency = balances.keys.elementAt(position)
        val amount = balances[currency]
        holder.bind(currency, amount)
    }

    override fun getItemCount(): Int {
        return balances.size
    }

    class BalanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val currencyTextView: TextView = itemView.findViewById(R.id.currency_text)
        private val amountTextView: TextView = itemView.findViewById(R.id.amount_text)

        fun bind(currency: String, amount: Double?) {
            currencyTextView.text = currency
            amountTextView.text = String.format("%.2f", amount)
        }
    }
}
