package com.example.memory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

//TODO:şifrelenebilecek krakter sayısını yazdır
//todo:karakter sayısı aşılmış mı kontrol et
//todo: mesaj yazıp şifrelediğinde fln bazı yerler telefon klavye altında kalıyor
//todo: mesaj coze tıklanınca memory yazıyor mu kontrolu yap

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