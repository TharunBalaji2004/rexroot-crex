package com.example.rexrootcrexapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupScreenActivity : AppCompatActivity() {

    private val parentLayout: View
        get() = findViewById(R.id.parent_layout)
    private val etFullName : EditText
        get() = findViewById(R.id.et_fullname)
    private val etEmailAddress : EditText
        get() = findViewById(R.id.et_emailaddress)
    private val etMobileNumber : EditText
        get() = findViewById(R.id.et_mobilenumber)
    private val etPassword : EditText
        get() = findViewById(R.id.et_password)
    private val btnSignUp : AppCompatButton
        get() = findViewById(R.id.btn_signup)
    private val tvClickHere : TextView
        get() = findViewById(R.id.tv_clickhere)
    private val progressBar : LinearLayout
        get() = findViewById(R.id.progress_bar)

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mStore : FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_screen)

        val currView = findViewById<View>(android.R.id.content)
        closeKeyBoard(currView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#000000")
        }

        mAuth = FirebaseAuth.getInstance()
        mStore = Firebase.firestore

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        btnSignUp.setOnClickListener {
            signUpNewUser()
        }

        tvClickHere.setOnClickListener {
            val intent = Intent(this@SignupScreenActivity,LoginScreenActivity::class.java)
            startActivity(intent)
        }

        val backgroundDrawable = resources.getDrawable(R.drawable.bg_inputbox)
        val focusedBackground = resources.getDrawable(R.drawable.bg_inputbox_focused)

        etFullName.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) etFullName.background = focusedBackground
            else etFullName.background = backgroundDrawable
        }
        etEmailAddress.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) etEmailAddress.background = focusedBackground
            else etEmailAddress.background = backgroundDrawable
        }
        etMobileNumber.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) etMobileNumber.background = focusedBackground
            else etMobileNumber.background = backgroundDrawable
        }
        etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) etPassword.background = focusedBackground
            else etPassword.background = backgroundDrawable
        }

    }

    private fun signUpNewUser(){

        val userFullName : String = etFullName.text.toString().trim()
        val userEmail : String = etEmailAddress.text.toString().trim()
        val userMobileNumber : String = etMobileNumber.text.toString().trim()
        val userPassword : String = etPassword.text.toString().trim()

        val formVaidate : Boolean = formValidation(userFullName,userEmail,userMobileNumber,userPassword)

        if (formVaidate){
            progressBar.visibility = View.VISIBLE
            mAuth.fetchSignInMethodsForEmail(userEmail).addOnCompleteListener{ task->

                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods

                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        // User Already has an account
                        Log.d("FirebaseAuth","(SUCCESSFUL) Checking Account already exists")

                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@SignupScreenActivity,"Account already exists",Toast.LENGTH_SHORT).show()
                    } else {
                        // User does not have account

                        mAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(this){ task->
                            if (task.isSuccessful) {
                                Log.d("FirebaseAuth","(SUCCESSFUL) Creating new account")

                                addNewUser(userFullName,userEmail,userMobileNumber)

                                progressBar.visibility = View.INVISIBLE
                                Toast.makeText(this@SignupScreenActivity, "Account created successfully",Toast.LENGTH_SHORT).show()

                                editor.putBoolean("isLoggedIn",true)
                                editor.putString("userEmailID",userEmail)
                                editor.commit()

                                val intent = Intent(this@SignupScreenActivity, OnboardingScreenActivity::class.java)
                                startActivity(intent)
                            } else {
                                Log.d("FirebaseAuth","(UNSUCCESSFUL) Creating new account")

                                progressBar.visibility = View.INVISIBLE
                                Toast.makeText(this@SignupScreenActivity, "Error occurred",Toast.LENGTH_SHORT).show()

                                editor.putBoolean("isLoggedIn",false)
                                editor.commit()
                            }
                        }

                    }
                } else {
                    Log.d("FirebaseAuth","(UNSUCCESSFUL) Checking account already exists")

                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@SignupScreenActivity, "Error occurred",Toast.LENGTH_SHORT).show()

                    editor.putBoolean("isLoggedIn",false)
                    editor.commit()
                }
            }
        }

    }

    private fun addNewUser(fullname:String,emailid:String,mobilenumber:String){
        val user = hashMapOf(
            "profiledata" to hashMapOf(
                "fullname" to fullname,
                "emailid" to emailid,
                "mobilenumber" to mobilenumber
            ),
            "submitdata" to hashMapOf<Any,Any>()
        )

        mStore.collection("users").add(user)
            .addOnSuccessListener {
                Log.d("FirestoreDB","Document Snapshot added with ID: ${it.id}")
            }
            .addOnFailureListener {
                Log.w("FirestoreDB","Error adding Document: ${it}")
            }
    }

    private fun formValidation(fullname: String,emailid: String,mobilenumber: String,password: String) : Boolean{
        var flag : Boolean = true
        val emailRegex = Regex("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$")

        if (fullname.isEmpty()) {
            etFullName.error = "Full Name is required"
            flag = false
        }
        if (emailid.isEmpty()) {
            etEmailAddress.error = "Email Address is required"
            flag = false
        }
        if (!emailid.matches(emailRegex)) {
            etEmailAddress.error = "Valid Email Address is required"
            flag = false
        }
        if (mobilenumber.isEmpty()) {
            etMobileNumber.error = "Mobile Number is required"
            flag = false
        }
        if (mobilenumber.length < 10 || mobilenumber.length > 10) {
            etMobileNumber.error = "Mobile Number should be of 10 digits"
            flag = false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            flag = false
        }

        return flag
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun closeKeyBoard(view: View) {
        parentLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val view = currentFocus
                if (view != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false
        }
    }
}