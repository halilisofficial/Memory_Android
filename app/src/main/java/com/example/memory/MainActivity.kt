package com.example.memory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


// bazı durumlarda uygulama cokebilio
//memory ile baslamiosa mesaj cözme hatası var

class MainActivity : AppCompatActivity() {

    private lateinit var btnNormal: Button
    private lateinit var btnSecure: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNormal = findViewById(R.id.btnNormal)
        btnSecure = findViewById(R.id.btnSecure)

        btnNormal.setOnClickListener {
            startActivity(Intent(this, NormalActivity::class.java))
        }

        btnSecure.setOnClickListener {
            startActivity(Intent(this, SecureActivity::class.java))
        }
    }
}