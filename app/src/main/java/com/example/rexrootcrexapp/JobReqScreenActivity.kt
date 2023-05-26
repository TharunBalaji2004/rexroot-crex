package com.example.rexrootcrexapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.UUID

class JobReqScreenActivity : AppCompatActivity() {
    private val PDF_REQUEST_CODE = 123
    private val db = FirebaseFirestore.getInstance()

    private var selectedFiles = mutableListOf<Uri>()
    private var selectedFilesNames = mutableListOf<String>()

    lateinit var ivExit : LinearLayout
    lateinit var tvHeaderJobRole : TextView
    lateinit var tvHeaderCompName : TextView
    lateinit var llBody : LinearLayout
    lateinit var scrollView : ScrollView
    lateinit var tvJobRole : TextView
    lateinit var tvCompName : TextView
    lateinit var tvPricePerClosure : TextView
    lateinit var tvJobDesc : TextView
    lateinit var btnSubmitResume : Button

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userDocumentId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jobreq_screen)

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        userDocumentId = sharedPreferences.getString("userDocumentId","").toString()
        Log.d("userDocumentId","userDocumentId: ${userDocumentId}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        ivExit = findViewById(R.id.iv_exit)
        tvHeaderJobRole = findViewById(R.id.tv_header_jobrole)
        tvHeaderCompName = findViewById(R.id.tv_header_compname)
        tvJobRole = findViewById(R.id.tv_jobrole)
        tvCompName = findViewById(R.id.tv_compname)
        tvPricePerClosure = findViewById(R.id.tv_priceperclosure)
        tvJobDesc = findViewById(R.id.tv_jobdesc)
        scrollView = findViewById(R.id.scroll_view)
        llBody = findViewById(R.id.ll_body)
        btnSubmitResume = findViewById(R.id.btn_submitresume)
        mediaPlayer = MediaPlayer.create(this@JobReqScreenActivity, R.raw.file_upload_success)

        ivExit.setOnClickListener {
            onBackPressed()
        }

        // Get data from previous Activity
        tvJobRole.text = intent.getStringExtra("jobRole")
        tvCompName.text = intent.getStringExtra("compName")
        tvPricePerClosure.text = intent.getStringExtra("pricePerClosure")
        tvJobDesc.text = intent.getStringExtra("jobDesc")

        tvHeaderJobRole.text = tvJobRole.text
        tvHeaderCompName.text = tvCompName.text
        btnSubmitResume.setBackgroundColor(Color.parseColor("#e51e26"))

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val heightllJobTitle = llBody.getChildAt(0).height

            if (scrollY >= heightllJobTitle) {
                tvHeaderJobRole.visibility = View.VISIBLE
                tvHeaderCompName.visibility = View.VISIBLE
            } else {
                tvHeaderJobRole.visibility = View.INVISIBLE
                tvHeaderCompName.visibility = View.INVISIBLE
            }
        }

        btnSubmitResume.setOnClickListener {
            val text : String = btnSubmitResume.text.toString()
            if (text == "Submit Resume"){
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_REQUEST_CODE)
            } else {
                btnSubmitResume.isEnabled = false
                btnSubmitResume.setBackgroundColor(Color.parseColor("#f06c71"))
                btnSubmitResume.text = "Uploading..."
                uploadPDFs(selectedFiles)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { singleUri ->
                // Handle single file selection
                selectedFiles.add(singleUri)
                selectedFilesNames.add(getFileNameFromUri(singleUri))
            }

            data?.clipData?.let { clipData ->
                // Handle multiple file selection
                for (i in 0 until clipData.itemCount) {
                    val clipDataItem = clipData.getItemAt(i)
                    selectedFiles.add(clipDataItem.uri)
                    selectedFilesNames.add(getFileNameFromUri(clipDataItem.uri))
                    Log.d("PDF UPLOAD","Uploaded file: ${getFileNameFromUri(clipDataItem.uri)}")
                }
            }

            if (selectedFiles.isNotEmpty()) {
                btnSubmitResume.text = "Upload Resume(s)"
            }
        }
    }

    private fun uploadPDFs(fileUris: List<Uri>) {
        val storageRef = Firebase.storage.reference

        fileUris.forEachIndexed { index, fileUri ->
            val fileName = UUID.randomUUID().toString() + ".pdf"
            val pdfRef = storageRef.child("pdfs/$fileName")

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
                }
            }
        }
    }

    private fun savePdfUrlToFirestore(downloadUrl: String, isLastFile: Boolean) {

        val userDocumentRef = db.collection("users").document(userDocumentId)
        Log.d("FirestoreDB","userDocumentRef: ${userDocumentRef}")
        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()){
                val data = documentSnapshot.data

                if (data != null){
                    val submitDataField = data["submitdata"] as HashMap<String,Any>

                    submitDataField["jobid"] = downloadUrl

                    userDocumentRef.update("submitdata",submitDataField)
                        .addOnSuccessListener {
                            Log.d("FirestoreDB","Document updated successful")
                        }
                        .addOnFailureListener {
                            Log.d("FirestoreDB","Document updated unsuccessful")
                        }
                } else {
                    Log.d("FirestoreDB","Data is null")
                }

            } else {
                Log.d("FirestoreDB","Document doesn't exist")
            }

        }

        if (isLastFile) {
            mediaPlayer.start()
            Toast.makeText(this@JobReqScreenActivity, "PDF(s) Uploaded Successfully!!", Toast.LENGTH_LONG).show()

            selectedFiles = mutableListOf<Uri>()
            selectedFilesNames = mutableListOf<String>()

            btnSubmitResume.setBackgroundColor(Color.parseColor("#e51e26"))
            btnSubmitResume.isEnabled = true
            btnSubmitResume.text = "Submit Resume"
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
}