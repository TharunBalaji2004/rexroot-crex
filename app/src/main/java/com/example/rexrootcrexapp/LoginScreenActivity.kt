package com.example.rexrootcrexapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
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
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginScreenActivity : AppCompatActivity() {

    private val parentLayout: View
        get() = findViewById(R.id.parent_layout)
    private val etEmailID: EditText
        get() = findViewById(R.id.et_emailaddress)
    private val etPassword: EditText
        get() = findViewById(R.id.et_password)
    private val btnLogIn: AppCompatButton
        get() = findViewById(R.id.btn_login)
    private val tvClickHere: TextView
        get() = findViewById(R.id.tv_clickhere)
    private val progressBar: LinearLayout
        get() = findViewById(R.id.progress_bar)

    private lateinit var mAuth: FirebaseAuth

    private var isClicked = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        val currView = findViewById<View>(android.R.id.content)
        closeKeyBoard(currView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#e51e26")
        }

        mAuth = FirebaseAuth.getInstance()

        btnLogIn.setOnClickListener {
            logInUser()
        }

        tvClickHere.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, SignupScreenActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logInUser() {

        val userEmailID: String = etEmailID.text.toString()
        val userPassword: String = etPassword.text.toString()

        val formValidate: Boolean = formValidation(userEmailID, userPassword)

        if (formValidate) {
            progressBar.visibility = View.VISIBLE
            mAuth.fetchSignInMethodsForEmail(userEmailID).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods

                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        // User Already has an account

                        mAuth.signInWithEmailAndPassword(userEmailID, userPassword)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    Log.d("FirebaseAuth", "(SUCCESSFUL) Account Login")

                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@LoginScreenActivity,
                                        "Account login successful",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent =
                                        Intent(this@LoginScreenActivity, MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Log.d("FirebaseAuth", "(UNSUCCESSFUL) Account Login")

                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@LoginScreenActivity,
                                        "Invalid email or password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // User does not have an account
                        Log.d("FirebaseAuth", "(SUCCESSFUL) Check Account already exists")

                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@LoginScreenActivity,
                            "Account does not exist",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Log.d("FirebaseAuth", "(UNSUCCESSFUL) Checking account already exists")

                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@LoginScreenActivity, "Error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun formValidation(emailid: String, password: String): Boolean {
        var flag: Boolean = true
        val emailRegex = Regex("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$")

        if (emailid.isEmpty()) {
            etEmailID.error = "Email Address is required"
            flag = false
        }
        if (!emailid.matches(emailRegex)) {
            etEmailID.error = "Valid Email Address is required"
            flag = false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            flag = false
        }

        return flag
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