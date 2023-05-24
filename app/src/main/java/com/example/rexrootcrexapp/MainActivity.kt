package com.example.rexrootcrexapp

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rexrootcrexapp.Data.JobReqDataClass
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val loadingProgressBar : ProgressBar
        get() = findViewById(R.id.pb_jobreq)

    private lateinit var recyclerView: RecyclerView
    private lateinit var jobReqList: ArrayList<JobReqDataClass>
    private lateinit var firebaseDB: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        recyclerView = findViewById(R.id.rv_jobreq)
        recyclerView.layoutManager = LinearLayoutManager(this)

        jobReqList = arrayListOf()
        loadingProgressBar.visibility = ProgressBar.VISIBLE

        firebaseDB = FirebaseDatabase.getInstance().getReference("root")

        firebaseDB.orderByKey().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    loadingProgressBar.visibility = ProgressBar.INVISIBLE
                    for (dataSnapshot in snapshot.children){
                        val jobReqCard = dataSnapshot.getValue(JobReqDataClass::class.java)
                        if (!jobReqList.contains(jobReqCard)){
                            jobReqList.add(jobReqCard!!) //null check
                        }
                    }
                    recyclerView.adapter = JobReqAdapter(jobReqList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseDB","Error Occured:"+error)
                Toast.makeText(this@MainActivity,"Database error occured", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}