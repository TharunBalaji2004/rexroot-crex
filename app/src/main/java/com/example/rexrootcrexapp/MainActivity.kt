package com.example.rexrootcrexapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rexrootcrexapp.Data.JobReqDataClass
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobReqAdapter
    private lateinit var jobReqList: ArrayList<JobReqDataClass>
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var ivMyProfile: ImageView
    private lateinit var fileName : String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userDocumentId : String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var jobId: String

    private val db = FirebaseFirestore.getInstance()
    private var selectedFilesNames = mutableListOf<String>()
    private var selectedUUIDFilesNames = mutableListOf<String>()
    private var selectedFiles = mutableListOf<Uri>()

    private val PICK_PDF_REQUEST = 1
    private var filePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#000000")
        }

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.rv_jobreq)
        recyclerView.layoutManager = LinearLayoutManager(this)

        jobReqList = arrayListOf()
        loadingProgressBar = findViewById(R.id.pb_jobreq)
        searchView = findViewById(R.id.sv_searchjobrole)
        ivMyProfile = findViewById(R.id.iv_myprofile)

        val firebaseDB = FirebaseDatabase.getInstance().getReference("root")
        val query = firebaseDB.orderByKey()
        val editor = sharedPreferences.edit()

        //setProfileImage()

        ivMyProfile.setOnClickListener {
            val intent = Intent(this@MainActivity,MyProfileScreenActivity::class.java)
            startActivity(intent)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    jobReqList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val dataSnapshotId = dataSnapshot.key
                        Log.d("dataSnapshotId","dataSnapshotId: ${dataSnapshotId}")
                        val jobReqCard = dataSnapshot.getValue(JobReqDataClass::class.java)
                        jobReqCard?.let { jobReqList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
                loadingProgressBar.visibility = ProgressBar.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseDB", "Error Occurred: $error")
                Toast.makeText(this@MainActivity, "Database error occurred", Toast.LENGTH_SHORT).show()
            }
        })

        adapter = JobReqAdapter(jobReqList,this@MainActivity)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("fileselection","Inside onActivityResult")
        selectedFiles = mutableListOf<Uri>()
        selectedUUIDFilesNames = mutableListOf<String>()
        selectedFilesNames = mutableListOf<String>()
        filePosition = 0

        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d("fileselection","requestCode condition satisfied")


            if (data?.clipData != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        selectedFiles.add(uri)
                    }
                }
            } else if (data?.data != null) {
                val uri = data.data
                if (uri != null) {
                    selectedFiles.add(uri)
                }
            }

            if (selectedFiles.isNotEmpty()) {
                Log.d("fileselection","$selectedFiles")

                val buttonPosition = sharedPreferences.getInt("buttonPosition",0)
                Log.d("buttonPosition",buttonPosition.toString())

                adapter.updateButtonText(buttonPosition, selectedFiles)
                uploadFiles(selectedFiles)
            }


        }
    }

    private fun uploadFiles(fileUris: List<Uri>) {
        val storageRef = Firebase.storage.reference

        jobId = sharedPreferences.getString("jobId","").toString()
        userDocumentId = sharedPreferences.getString("userDocumentId","").toString()

        Log.d("userDocumentId",userDocumentId)

        fileUris.forEachIndexed { index, fileUri ->
            fileName = UUID.randomUUID().toString()
            val pdfRef = storageRef.child("$userDocumentId/$jobId/${fileName}.pdf")

            selectedUUIDFilesNames.add(fileName)
            selectedFilesNames.add(getFileNameFromUri(fileUri))

            Log.d("selectedFilesNames","$selectedFilesNames")

            val uploadTask = pdfRef.putFile(fileUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("PDF upload failed")
                }
                pdfRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    savePdfUrlToFirestore(downloadUrl.toString(), index == fileUris.size - 1)
                } else {
                    // Handle the upload failure
                    Log.e("FirestoreDB","PDF Upload failure")
                }
            }
        }
    }

    private fun savePdfUrlToFirestore(downloadUrl: String, isLastFile: Boolean) {

        Log.d("savepdfurltofirestore", userDocumentId)

        val userDocumentRef = db.collection("users").document(userDocumentId)
        jobId = sharedPreferences.getString("jobId","").toString()

        Log.d("selectedFilesNames","$selectedFilesNames")

        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                val timeFormat = Date()
                val dateFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
                val currTime = dateFormat.format(timeFormat)

                val UUIDFileName = selectedUUIDFilesNames[filePosition]
                val fileId = currTime

                val newResumeData = hashMapOf<String, Any>(
                    "submitdata" to hashMapOf<String, Any>(
                        jobId to hashMapOf<String, Any>(
                            fileId+UUIDFileName to hashMapOf(
                                "resumeId" to UUIDFileName,
                                "resumeName" to selectedFilesNames[filePosition],
                                "resumeUrl" to downloadUrl,
                                "resumeStatus" to "0"
                            )
                        )
                    )
                )

                filePosition++

                userDocumentRef.set(newResumeData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("FirestoreDB", "Filename: ${UUIDFileName}")
                        Log.d("FirestoreDB", "Document added successfully: ${downloadUrl}")
                        if (isLastFile) {
                            Log.d("FirestoreDB", "All files uploaded and documents updated successfully")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.d("FirestoreDB", "Document update failed: ${e.message}")
                    }
            } else {
                Log.d("FirestoreDB", "Document doesn't exist")
            }
        }

        if (isLastFile) {
            mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.file_upload_success)
            mediaPlayer.start()
            Toast.makeText(this@MainActivity, "PDF(s) Uploaded Successfully!!", Toast.LENGTH_LONG).show()

            selectedFiles = mutableListOf<Uri>()
            val buttonPosition = sharedPreferences.getInt("buttonPosition",0)
            adapter.updateButtonText(buttonPosition, selectedFiles)
        }

    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName: String? = null
        val scheme = uri.scheme
        if (scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        return fileName ?: "N/A"
    }

    private fun setProfileImage() {
        val profileImageUrl: String? = sharedPreferences.getString("imageUrl","")

        Glide.with(this)
            .load(profileImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(ivMyProfile)
    }

    override fun onBackPressed() {
        if (searchView.query.toString().isEmpty()){
            finishAffinity()
        }
        if (!searchView.isIconified) {
            searchView.setQuery("", false)
            searchView.isIconified = true
        }
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
    }

}