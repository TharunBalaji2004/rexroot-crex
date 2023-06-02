package com.example.rexrootcrexapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rexrootcrexapp.databinding.MyprofileScreenBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MyProfileScreenActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var userDocumentId: String
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

        //setProfileImage()
        userDocumentId = sharedPreferences.getString("userDocumentId","").toString()
        binding.tvProfilename.text = sharedPreferences.getString("fullName","N/A")
        binding.tvEmail.text = sharedPreferences.getString("emailId","N/A")
        binding.tvMobilenumber.text = sharedPreferences.getString("mobileNumber","N/A")
        binding.btnLogout.setBackgroundColor(Color.RED)

        binding.llGoback.setOnClickListener {
            onBackPressed()
        }
        binding.btnLogout.setOnClickListener {
            editor.putBoolean("isLoggedIn",false)
            editor.commit()

            val intent = Intent(this@MyProfileScreenActivity,LoginScreenActivity::class.java)
            startActivity(intent)
        }
//        binding.ivProfileimage.setOnClickListener {
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
//        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
//            val imageUri = data?.data
//
//            if (imageUri != null) {
//                binding.ivProfileimage.setImageURI(imageUri)
//                uploadImage(imageUri)
//            }
//        }
//    }
//
//    private fun uploadImage(imageUri: Uri) {
//        val storageRef = Firebase.storage.reference
//
//        val imageRef = storageRef.child("$userDocumentId/profiledata/profile_image")
//
//        val uploadTask = imageRef.putFile(imageUri)
//
//        uploadTask.continueWithTask { task ->
//                if (!task.isSuccessful) {
//                    throw task.exception ?: Exception("PDF upload failed")
//                }
//                imageRef.downloadUrl
//            }.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val downloadUrl = task.result
//                saveImageUrlToFirestore(downloadUrl.toString())
//            } else {
//                // Handle the upload failure
//            }
//        }
//    }
//
//    private fun saveImageUrlToFirestore(downloadUrl: String) {
//        val db = FirebaseFirestore.getInstance()
//        val userDocumentRef = db.collection("users").document(userDocumentId)
//
//        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
//            if (documentSnapshot.exists()) {
//
//                val profileData = (documentSnapshot.get("profiledata") as? Map<String,Any>)?.toMutableMap()
//
//                Log.d("profileData", profileData.toString())
//                Log.d("downloadUrl",downloadUrl)
//
//                if (profileData != null) {
//                    profileData["imageurl"] = downloadUrl
//                }
//
//                if (profileData != null) {
//                    userDocumentRef.set(profileData, SetOptions.merge())
//                        .addOnSuccessListener {
//                            Log.d("FirestoreDB","Image URL added")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.d("FirestoreDB", "Image URL failed: ${e.message}")
//                        }
//                }
//
//                Toast.makeText(this@MyProfileScreenActivity, "Profile Image changed Successfully!!", Toast.LENGTH_LONG).show()
//
//            } else {
//                Log.d("FirestoreDB", "Document doesn't exist")
//            }
//        }
//    }

    private fun setProfileImage() {
        val profileImageUrl: String? = sharedPreferences.getString("imageUrl","")

        Glide.with(this)
            .load(profileImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivProfileimage)
    }

}