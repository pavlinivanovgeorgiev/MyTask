package com.homework.mytask.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.homework.mytask.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CurrencyConverterFragment())
                .commit()
        }
    }
}