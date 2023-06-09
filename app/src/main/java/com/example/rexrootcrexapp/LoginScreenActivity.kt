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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        val currView = findViewById<View>(android.R.id.content)
        closeKeyBoard()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#000000")
        }

        mAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        btnLogIn.setOnClickListener {
            logInUser()
        }

        tvClickHere.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, SignupScreenActivity::class.java)
            startActivity(intent)
        }

        val backgroundDrawable = resources.getDrawable(R.drawable.bg_inputbox)
        val focusedBackground = resources.getDrawable(R.drawable.bg_inputbox_focused)
        etEmailID.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) etEmailID.background = focusedBackground
            else etEmailID.background = backgroundDrawable
        }
        etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) etPassword.background = focusedBackground
            else etPassword.background = backgroundDrawable
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

                                    findUsersDocumentRef(userEmailID)
                                    getProfileData()

                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@LoginScreenActivity,
                                        "Account login successful",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val user = mAuth.currentUser
                                    Log.d("currentUser", user.toString())

                                    editor.putBoolean("isLoggedIn",true)
                                    editor.putString("userEmailID",userEmailID)
                                    editor.commit()

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

                                    editor.putBoolean("isLoggedIn",false)
                                    editor.commit()

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

                        editor.putBoolean("isLoggedIn",false)
                        editor.commit()

                    }

                } else {
                    Log.d("FirebaseAuth", "(UNSUCCESSFUL) Checking account already exists")

                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@LoginScreenActivity, "Error occurred", Toast.LENGTH_SHORT)
                        .show()

                    editor.putBoolean("isLoggedIn",false)
                    editor.commit()

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

    private fun findUsersDocumentRef(userEmailID: String) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("users")

        collectionRef
            .whereEqualTo("profiledata.emailid", userEmailID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    editor.putString("userDocumentId", document.id)
                    Log.d("userDocumentId (LOGIN)","userDocumentId: ${document.id}")
                    editor.commit()
                } else {
                    Log.d("FirestoreDB","Profile Data not found")
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred
            }
    }

    private fun closeKeyBoard() {
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

    private fun getProfileData() {
        val userDocumentId: String? = sharedPreferences.getString("userDocumentId","")
        val userDocumentRef = userDocumentId?.let { db.collection("users").document(it) }

        Log.d("userDocumentId (LOGIN)",userDocumentId.toString())

        if (userDocumentRef != null) {
            userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val profileData = documentSnapshot.get("profiledata") as? Map<*,*>
                    Log.d("profiledata",profileData.toString())

                    val imageUrl: Any? = profileData?.get("imageurl")
                    val emailId: Any? = profileData?.get("emailid")
                    val fullName: Any? = profileData?.get("fullname")
                    val mobileNumber: Any? = profileData?.get("mobilenumber")

                    editor.putString("imageUrl",imageUrl.toString())
                    editor.putString("emailId",emailId.toString())
                    editor.putString("fullName",fullName.toString())
                    editor.putString("mobileNumber",mobileNumber.toString())
                    editor.commit()
                } else {
                    Log.d("FirestoreDB","Document Snapshot does not exists")
                }
            }
        } else {
            // userdocument does not exist
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}