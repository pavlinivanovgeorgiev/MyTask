package com.homework.mytask.view

import CurrencyViewModel
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.homework.mytask.R


class CurrencyConverterFragment : Fragment() {
    private val viewModel: CurrencyViewModel by viewModels()
    lateinit var fromCurrencySpinner: Spinner
    lateinit var toCurrencySpinner: Spinner
    lateinit var balanceAdapter: BalanceAdapter
    lateinit var receiveAmountText: TextView
    lateinit var amountInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_currency_converter, container, false)

        amountInput = root.findViewById(R.id.amount_input)
        fromCurrencySpinner = root.findViewById(R.id.from_currency_spinner)
        toCurrencySpinner = root.findViewById(R.id.to_currency_spinner)
        receiveAmountText = root.findViewById(R.id.tv_receive_amount)
        val convertButton: Button = root.findViewById(R.id.convert_button)
        val balanceRecyclerView: RecyclerView = root.findViewById(R.id.balance_recycler_view)

        // Set up RecyclerView for displaying positive balances
        balanceRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        balanceAdapter = BalanceAdapter()
        balanceRecyclerView.adapter = balanceAdapter

        initObservers()

        convertButton.setOnClickListener {
            val amount = amountInput.text.toString().toDoubleOrNull()
            val fromCurrencyName = fromCurrencySpinner.selectedItem as String
            val toCurrencyName = toCurrencySpinner.selectedItem as String
            val fromCurrency = Currency.fromName(fromCurrencyName)
            val toCurrency = Currency.fromName(toCurrencyName)

            if (amount != null && fromCurrency != null && toCurrency != null) {
                try {
                    viewModel.convertCurrency(amount, fromCurrency, toCurrency)
                } catch (e: IllegalArgumentException) {
                    showDialog(
                        getString(R.string.insufficient_balance),
                        getString(R.string.insufficient_balance_for_the_transaction)
                    )
                }
            }
        }

        // Add listener to calculate receive amount after sell currency change
        amountInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateReceiveAmount()
            }
        })

        // Add listeners for currency selection changes
        fromCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                calculateReceiveAmount()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        toCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                calculateReceiveAmount()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return root
    }

    private fun calculateReceiveAmount(){
        val amount = amountInput.text.toString().toDoubleOrNull()
        val fromCurrencyName = fromCurrencySpinner.selectedItem as String
        val toCurrencyName = toCurrencySpinner.selectedItem as String
        val fromCurrency = Currency.fromName(fromCurrencyName)
        val toCurrency = Currency.fromName(toCurrencyName)

        if (amount != null && fromCurrency != null && toCurrency != null) {
            viewModel.calculateReceiveAmount(amount, fromCurrency, toCurrency)
        }
    }

    private fun initObservers(){
        // Observer for updating the balance list
        viewModel.balance.observe(viewLifecycleOwner, Observer { balanceValue ->
            val positiveBalances = viewModel.getPositiveBalances()
            balanceAdapter.updateBalances(positiveBalances)
        })

        // Observer for updating currency rates and reloading the spinners
        viewModel.exchangeRates.observe(viewLifecycleOwner, Observer {
            val updatedCurrencies = Currency.getAllCurrencies().map { it.name }
            val updatedAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                updatedCurrencies
            )
            updatedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            fromCurrencySpinner.adapter = updatedAdapter
            toCurrencySpinner.adapter = updatedAdapter
        })

        // observe to show dialog after convert currency
        viewModel.convertedValue.observe(viewLifecycleOwner, Observer { convertedValue ->
            showDialog(getString(R.string.currency_converted), convertedValue)
        })

        // observe sell currency amount to calculate receive currency amount in real time
        viewModel.receiveAmount.observe(viewLifecycleOwner, Observer { receiveAmount ->
            receiveAmountText.text = String.format("%.2f", receiveAmount)
        })
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
