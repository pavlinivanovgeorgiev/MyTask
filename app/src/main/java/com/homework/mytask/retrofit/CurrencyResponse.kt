package com.homework.task.retrofit

data class CurrencyResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)