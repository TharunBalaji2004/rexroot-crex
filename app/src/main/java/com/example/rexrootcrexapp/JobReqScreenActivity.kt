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
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class JobReqScreenActivity : AppCompatActivity() {
    private val PDF_REQUEST_CODE = 123
    private val db = FirebaseFirestore.getInstance()

    private var selectedFiles = mutableListOf<Uri>()
    private var selectedFilesNames = mutableListOf<String>()
    private var selectedUUIDFilesNames = mutableListOf<String>()
    private var submittedList: ArrayList<String> = arrayListOf<String>()
    private var rejectedList: ArrayList<String> = arrayListOf<String>()
    private var acceptedList: ArrayList<String> = arrayListOf<String>()

    private var filePosition = 0
    private var submittedCount = 0
    private var rejectedCount = 0
    private var acceptedCount = 0

    lateinit var ivExit : LinearLayout
    lateinit var tvHeaderJobRole : TextView
    lateinit var tvHeaderCompName : TextView
    lateinit var llBody : LinearLayout
    lateinit var llJobTitle : LinearLayout
    lateinit var scrollView : ScrollView
    lateinit var tvJobRole : TextView
    lateinit var tvCompName : TextView
    lateinit var tvPricePerClosure : TextView
    lateinit var tvCompLocation : TextView
    lateinit var tvJobType : TextView
    lateinit var tvJobSkills : TextView
    lateinit var tvJobDesc : TextView
    lateinit var tvShowMore : TextView
    lateinit var tvSubmitted : TextView
    lateinit var tvRejected : TextView
    lateinit var tvAccepted : TextView
    lateinit var btnSubmitResume : Button
    lateinit var btnUploadResume : Button
    lateinit var vElevation : View
    lateinit var rlSubmitted : RelativeLayout
    lateinit var rlRejected : RelativeLayout
    lateinit var rlAccepted : RelativeLayout
    lateinit var rvSubmitted : RecyclerView
    lateinit var tvSSubText : TextView
    lateinit var rvRejected : RecyclerView
    lateinit var tvRSubText : TextView
    lateinit var rvAccepted : RecyclerView
    lateinit var tvASubText : TextView
    lateinit var shimmerSubmitted : ShimmerFrameLayout
    lateinit var shimmerRejected : ShimmerFrameLayout
    lateinit var shimmerAccepted : ShimmerFrameLayout
    lateinit var llSubmitted : LinearLayout
    lateinit var llRejected : LinearLayout
    lateinit var llAccepted : LinearLayout

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userDocumentId : String
    private lateinit var jobId : String
    private lateinit var fileName : String
    private lateinit var submittedAdapter: SubmissionsAdapter
    private lateinit var rejectedAdapter: SubmissionsAdapter
    private lateinit var acceptedAdapter: SubmissionsAdapter

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

        tvCompLocation = findViewById(R.id.tv_companylocation)
        tvJobType = findViewById(R.id.tv_jobtype)
        tvJobSkills = findViewById(R.id.tv_jobskills)

        tvJobDesc = findViewById(R.id.tv_jobdesc)
        tvShowMore= findViewById(R.id.tv_showmore)
        scrollView = findViewById(R.id.scroll_view)
        llBody = findViewById(R.id.ll_body)
        llJobTitle = findViewById(R.id.ll_jobtitle)
        tvSubmitted = findViewById(R.id.tv_submitted)
        tvRejected = findViewById(R.id.tv_rejected)
        tvAccepted = findViewById(R.id.tv_accepted)
        btnSubmitResume = findViewById(R.id.btn_submitresume)
        btnUploadResume = findViewById(R.id.btn_uploadresume)
        vElevation = findViewById(R.id.v_elevation)
        rlSubmitted = findViewById(R.id.rl_submitted)
        rlRejected = findViewById(R.id.rl_rejected)
        rlAccepted = findViewById(R.id.rl_accepted)
        rvSubmitted = findViewById(R.id.rv_submitted)
        tvSSubText = findViewById(R.id.tv_ssubtext)
        rvRejected = findViewById(R.id.rv_rejected)
        tvRSubText = findViewById(R.id.tv_rsubtext)
        rvAccepted = findViewById(R.id.rv_accepted)
        tvASubText = findViewById(R.id.tv_asubtext)

        shimmerSubmitted = findViewById(R.id.shimmer_submitted)
        shimmerRejected = findViewById(R.id.shimmer_rejected)
        shimmerAccepted = findViewById(R.id.shimmer_accepted)

        llSubmitted = findViewById(R.id.ll_submitted)
        llRejected = findViewById(R.id.ll_rejected)
        llAccepted = findViewById(R.id.ll_accepted)

        mediaPlayer = MediaPlayer.create(this@JobReqScreenActivity, R.raw.file_upload_success)

        ivExit.setOnClickListener {
            onBackPressed()
        }

        refreshSubmissions()

        // Get data from previous Activity
        jobId = intent.getStringExtra("jobId").toString()
        tvJobRole.text = intent.getStringExtra("jobRole")
        tvCompName.text = intent.getStringExtra("compName")
        tvPricePerClosure.text = intent.getStringExtra("pricePerClosure")
        tvCompLocation.text = intent.getStringExtra("compLocation")
        tvJobType.text = intent.getStringExtra("jobType")
        tvJobSkills.text = intent.getStringExtra("jobSkills")
        tvJobDesc.text = intent.getStringExtra("jobDesc")
        val jobDesc: String? = intent.getStringExtra("jobDesc")


        if (tvJobDesc.text.length >= 150) {
            tvJobDesc.text = jobDesc?.substring(0,150) + "..."
        } else {
            tvJobDesc.text = jobDesc
            tvShowMore.visibility = View.GONE
        }

        tvHeaderJobRole.text = tvJobRole.text
        tvHeaderCompName.text = tvCompName.text
        btnSubmitResume.setBackgroundColor(Color.parseColor("#e51e26"))
        btnUploadResume.setBackgroundColor(Color.parseColor("#f06c71"))
        btnUploadResume.isEnabled = false

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val heightllJobRole = llJobTitle.getChildAt(0).height

            if (scrollY >= heightllJobRole) {
                tvHeaderJobRole.visibility = View.VISIBLE
                tvHeaderCompName.visibility = View.VISIBLE
                vElevation.visibility = View.VISIBLE
            } else {
                tvHeaderJobRole.visibility = View.INVISIBLE
                tvHeaderCompName.visibility = View.INVISIBLE
                vElevation.visibility = View.INVISIBLE
            }
        }

        btnSubmitResume.setOnClickListener {
            selectedFiles = mutableListOf<Uri>()
            selectedUUIDFilesNames = mutableListOf<String>()
            selectedFilesNames = mutableListOf<String>()
            filePosition = 0

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_REQUEST_CODE)
        }

        btnUploadResume.setOnClickListener {
            btnSubmitResume.isEnabled = false
            btnSubmitResume.setBackgroundColor(Color.parseColor("#f06c71"))
            btnUploadResume.isEnabled = false
            btnUploadResume.setBackgroundColor(Color.parseColor("#f06c71"))
            btnUploadResume.text = "Uploading..."

            Log.d("selectedfiles","$selectedFiles")
            Log.d("selectedFilesNames","$selectedFilesNames")

            uploadPDFs(selectedFiles)
        }

        tvShowMore.setOnClickListener {
            val text = tvShowMore.text.toString()
            if (text == "Show more") {
                tvShowMore.text = "Show less"
                tvJobDesc.text = jobDesc
            } else {
                tvShowMore.text = "Show more"
                tvJobDesc.text = jobDesc?.substring(0,150) + "..."
            }
        }

        submittedAdapter = SubmissionsAdapter(submittedList)
        rvSubmitted.layoutManager = LinearLayoutManager(this)
        rvSubmitted.adapter = submittedAdapter
        rejectedAdapter = SubmissionsAdapter(rejectedList)
        rvRejected.layoutManager = LinearLayoutManager(this)
        rvRejected.adapter = rejectedAdapter
        acceptedAdapter = SubmissionsAdapter(acceptedList)
        rvAccepted.layoutManager = LinearLayoutManager(this)
        rvAccepted.adapter = acceptedAdapter

        rlSubmitted.setOnClickListener {
            if (rvSubmitted.visibility == View.VISIBLE || tvSSubText.visibility == View.VISIBLE){
                rvSubmitted.visibility = View.GONE
                tvSSubText.visibility = View.GONE
            }
            else if (submittedList.size == 0){
                rvSubmitted.visibility = View.GONE
                tvSSubText.visibility = View.VISIBLE
            } else {
                tvSSubText.visibility = View.GONE
                rvSubmitted.visibility = View.VISIBLE
            }
        }

        rlRejected.setOnClickListener {
            if (rvRejected.visibility == View.VISIBLE || tvRSubText.visibility == View.VISIBLE){
                rvRejected.visibility = View.GONE
                tvRSubText.visibility = View.GONE
            }
            else if (rejectedList.size == 0){
                rvRejected.visibility = View.GONE
                tvRSubText.visibility = View.VISIBLE
            } else {
                tvRSubText.visibility = View.GONE
                rvRejected.visibility = View.VISIBLE
            }
        }

        rlAccepted.setOnClickListener {
            if (rvAccepted.visibility == View.VISIBLE || tvASubText.visibility == View.VISIBLE){
                rvAccepted.visibility = View.GONE
                tvASubText.visibility = View.GONE
            }
            else if (acceptedList.size == 0){
                rvAccepted.visibility = View.GONE
                tvASubText.visibility = View.VISIBLE
            } else {
                tvASubText.visibility = View.GONE
                rvAccepted.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { singleUri ->
                // Handle single file selection
                selectedFiles.add(singleUri)
            }

            data?.clipData?.let { clipData ->
                // Handle multiple file selection
                for (i in 0 until clipData.itemCount) {
                    val clipDataItem = clipData.getItemAt(i)
                    selectedFiles.add(clipDataItem.uri)
                }
            }

            if (selectedFiles.isNotEmpty()) {
                btnUploadResume.isEnabled = true
                btnUploadResume.setBackgroundColor(Color.parseColor("#e51e26"))
                btnSubmitResume.text = "Reselect Resume"
            }
        }
    }

    private fun uploadPDFs(fileUris: List<Uri>) {
        val storageRef = Firebase.storage.reference

        fileUris.forEachIndexed { index, fileUri ->
            fileName = UUID.randomUUID().toString()
            val pdfRef = storageRef.child("$userDocumentId/submitdata/$jobId/${fileName}")

            selectedUUIDFilesNames.add(fileName)
            selectedFilesNames.add(getFileNameFromUri(fileUri))


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

            Handler(Looper.getMainLooper()).postDelayed({
                refreshSubmissions()
            },500)

            mediaPlayer.start()
            Toast.makeText(this@JobReqScreenActivity, "PDF(s) Uploaded Successfully!!", Toast.LENGTH_LONG).show()

            btnUploadResume.setBackgroundColor(Color.parseColor("#e51e26"))
            btnUploadResume.isEnabled = true
            btnUploadResume.text = "Upload"
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

    private fun refreshSubmissions() {
        shimmerSubmitted.visibility = View.VISIBLE
        shimmerAccepted.visibility = View.VISIBLE
        shimmerRejected.visibility = View.VISIBLE

        llSubmitted.visibility = View.GONE
        llAccepted.visibility = View.GONE
        llRejected.visibility = View.GONE

        shimmerSubmitted.startShimmer()
        shimmerAccepted.startShimmer()
        shimmerRejected.startShimmer()

        val userDocumentRef = db.collection("users").document(userDocumentId)

        submittedCount = 0
        rejectedCount = 0
        acceptedCount = 0

        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()){

                val submitData = documentSnapshot.get("submitdata") as? Map<*,*>
                Log.d("submitdata",submitData.toString())

                if (submitData != null) {
                    val uploadedResumes = submitData[jobId] as? Map<*, *>

                    Log.d("uploadedResumes", uploadedResumes.toString())

                    if (uploadedResumes != null) {
                        var resumeProcessedCount = 0

                        for (itemResume in uploadedResumes.keys){

                            val resumeData = uploadedResumes[itemResume] as? Map<*, *>
                            val resumeStatus = resumeData?.get("resumeStatus")
                            val resumeName = resumeData?.get("resumeName").toString()

                            if (resumeStatus == "0") {
                                submittedList.add(resumeName)
                                submittedCount++
                            }
                            if (resumeStatus == "1") {
                                acceptedList.add(resumeName)
                                acceptedCount++
                            }
                            if (resumeStatus == "-1") {
                                rejectedList.add(resumeName)
                                rejectedCount++
                            }

                            Log.d("submittedList","$submittedList")
                            Log.d("rejectedList","$rejectedList")
                            Log.d("acceptedList","$acceptedList")

                            resumeProcessedCount++

                            if (resumeProcessedCount == uploadedResumes.size){
                                tvSubmitted.text = submittedCount.toString()
                                tvRejected.text = rejectedCount.toString()
                                tvAccepted.text = acceptedCount.toString()


                                submittedAdapter.notifyDataSetChanged()
                                rejectedAdapter.notifyDataSetChanged()
                                acceptedAdapter.notifyDataSetChanged()

                            }
                        }
                    }
                }
            } else {
                 // Document not exists
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            shimmerSubmitted.stopShimmer()
            shimmerAccepted.stopShimmer()
            shimmerRejected.stopShimmer()

            shimmerSubmitted.visibility = View.GONE
            shimmerAccepted.visibility = View.GONE
            shimmerRejected.visibility = View.GONE

            llSubmitted.visibility = View.VISIBLE
            llAccepted.visibility = View.VISIBLE
            llRejected.visibility = View.VISIBLE

            Log.d("submittedCount", submittedCount.toString())
            Log.d("rejectedCount", rejectedCount.toString())
            Log.d("acceptedCount", acceptedCount.toString())

        }, 3000)
    }
}