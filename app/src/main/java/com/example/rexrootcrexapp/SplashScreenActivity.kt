package com.example.rexrootcrexapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    private val splashTimeOut : Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
        }

        Handler().postDelayed({
            val intent = Intent(this@SplashScreenActivity,SignupScreenActivity::class.java)
            startActivity(intent)
        }, splashTimeOut)
    }
}