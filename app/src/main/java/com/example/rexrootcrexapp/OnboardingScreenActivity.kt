package com.example.rexrootcrexapp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class OnboardingScreenActivity : AppCompatActivity() {

    private val ivRecruiter : ImageView
        get() = findViewById(R.id.iv_recruiter)
    private val ivCandidate : ImageView
        get() = findViewById(R.id.iv_candidate)
    private val ivFreeRec : ImageView
        get() = findViewById(R.id.iv_freerec)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#e51e26")
        }

        ivRecruiter.setOnClickListener {
            val intent = Intent(this@OnboardingScreenActivity,MainActivity::class.java)
            startActivity(intent)
        }

        ivCandidate.setOnClickListener {
            val intent = Intent(this@OnboardingScreenActivity,MainActivity::class.java)
            startActivity(intent)
        }

        ivFreeRec.setOnClickListener {
            val intent = Intent(this@OnboardingScreenActivity,MainActivity::class.java)
            startActivity(intent)
        }
    }
}