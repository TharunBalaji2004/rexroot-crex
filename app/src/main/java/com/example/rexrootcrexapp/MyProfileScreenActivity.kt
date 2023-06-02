package com.example.rexrootcrexapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rexrootcrexapp.databinding.MyprofileScreenBinding

class MyProfileScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: MyprofileScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MyprofileScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#FF0000")
        }

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        binding.llGoback.setOnClickListener {
            onBackPressed()
        }
        binding.btnLogout.setOnClickListener {
            editor.putBoolean("isLoggedIn",false)
            editor.commit()

            val intent = Intent(this@MyProfileScreenActivity,LoginScreenActivity::class.java)
            startActivity(intent)
        }

        binding.tvProfilename.text = sharedPreferences.getString("fullName","N/A")
        binding.tvEmail.text = sharedPreferences.getString("emailId","N/A")
        binding.tvMobilenumber.text = sharedPreferences.getString("mobileNumber","N/A")
        binding.btnLogout.setBackgroundColor(Color.RED)

    }
}