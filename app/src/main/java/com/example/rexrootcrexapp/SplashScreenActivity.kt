package com.example.rexrootcrexapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    private val splashTimeOut : Long = 1000
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
        }

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        Handler().postDelayed({

            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false)

            if (isLoggedIn) {
                val intent = Intent(this@SplashScreenActivity,MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@SplashScreenActivity,LoginScreenActivity::class.java)
                startActivity(intent)
            }

        }, splashTimeOut)
    }
}