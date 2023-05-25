package com.example.rexrootcrexapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class JobReqScreenActivity : AppCompatActivity() {

    lateinit var ivExit : LinearLayout
    lateinit var tvHeaderJobRole : TextView
    lateinit var tvHeaderCompName : TextView
    lateinit var llBody : LinearLayout
    lateinit var scrollView : ScrollView
    lateinit var tvJobRole : TextView
    lateinit var tvCompName : TextView
    lateinit var tvPricePerClosure : TextView
    lateinit var tvJobDesc : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jobreq_screen)

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
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}