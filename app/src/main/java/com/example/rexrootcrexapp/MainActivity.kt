package com.example.rexrootcrexapp

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobReqAdapter
    private lateinit var jobReqList: ArrayList<JobReqDataClass>
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        recyclerView = findViewById(R.id.rv_jobreq)
        recyclerView.layoutManager = LinearLayoutManager(this)

        jobReqList = arrayListOf()
        loadingProgressBar = findViewById(R.id.pb_jobreq)
        searchView = findViewById(R.id.sv_searchjobrole)

        val firebaseDB = FirebaseDatabase.getInstance().getReference("root")
        val query = firebaseDB.orderByKey()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    jobReqList.clear()
                    for (dataSnapshot in snapshot.children) {
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
}