package com.example.rexrootcrexapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.example.rexrootcrexapp.Data.JobReqDataClass
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobReqAdapter
    private lateinit var jobReqList: ArrayList<JobReqDataClass>
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var ivLogOut: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        recyclerView = findViewById(R.id.rv_jobreq)
        recyclerView.layoutManager = LinearLayoutManager(this)

        jobReqList = arrayListOf()
        loadingProgressBar = findViewById(R.id.pb_jobreq)
        searchView = findViewById(R.id.sv_searchjobrole)
        ivLogOut = findViewById(R.id.iv_logout)

        val firebaseDB = FirebaseDatabase.getInstance().getReference("root")
        val query = firebaseDB.orderByKey()

        val sharedPreferences = getSharedPreferences("UserPreferences",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        findUsersDocumentRef()

        ivLogOut.setOnClickListener {
            editor.putBoolean("isLoggedIn",false)
            editor.commit()

            val intent = Intent(this@MainActivity,LoginScreenActivity::class.java)
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

        adapter = JobReqAdapter(jobReqList)
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
    }

    private fun findUsersDocumentRef() {
        val db = FirebaseFirestore.getInstance()

        val collectionRef = db.collection("users")

        val sharedPreferences = getSharedPreferences("UserPreferences",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val email = sharedPreferences.getString("userEmailID","")
        Log.d("FirestoreDB","UserEmail: ${email}")

        collectionRef
            .whereEqualTo("profiledata.emailid", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    editor.putString("userDocumentId", document.id)
                    Log.d("userDocumentId","userDocumentId: ${document.id}")
                    editor.commit()
                } else {
                    Log.d("FirestoreDB","Profile Data not found")
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred
            }

    }
}