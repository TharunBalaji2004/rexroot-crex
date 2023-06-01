package com.example.rexrootcrexapp

import android.app.Activity
import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.example.rexrootcrexapp.Data.JobReqDataClass
import java.util.Locale

class JobReqAdapter(private val jobReqList : ArrayList<JobReqDataClass>, private val context: Context) : RecyclerView.Adapter<JobReqAdapter.JobReqViewHolder>(), Filterable {

    private var filteredList: List<JobReqDataClass> = jobReqList
    private val selectedFilesMap: MutableMap<Int, List<Uri>> = mutableMapOf()
    private val PICK_PDF_REQUEST = 1

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = sharedPreferences.edit()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobReqViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.jobreq_card, parent, false)
        return JobReqViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobReqViewHolder, position: Int) {
        val currentItem = filteredList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase(Locale.getDefault())
                val filteredItems = if (query.isNullOrEmpty()) {
                    jobReqList
                } else {
                    jobReqList.filter { item ->
                        item.jobRole?.lowercase(Locale.getDefault())?.contains(query) == true ||
                                item.companyName?.lowercase(Locale.getDefault())?.contains(query) == true
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredItems
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<JobReqDataClass> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    fun updateButtonText(position: Int, selectedFiles: List<Uri>) {
        if (selectedFiles.isNotEmpty()){
            selectedFilesMap[position] = selectedFiles
        } else {
            selectedFilesMap.clear()
        }
        notifyItemChanged(position)
    }

    inner class JobReqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobRole: TextView = itemView.findViewById(R.id.tv_jobrole)
        val compName: TextView = itemView.findViewById(R.id.tv_compname)
        val jobSkills: TextView = itemView.findViewById(R.id.tv_jobskills)
        val pricePerClosure: TextView = itemView.findViewById(R.id.tv_priceperclosure)
        val btnSubmitResume: LinearLayout = itemView.findViewById(R.id.btn_submitresume)
        val tvButtonText: TextView = itemView.findViewById(R.id.tv_buttontext)
        val tvJobSubText: TextView = itemView.findViewById(R.id.tv_jobsubtext)

        fun bind(item: JobReqDataClass) {
            jobRole.text = item.jobRole
            compName.text = item.companyName
            pricePerClosure.text = "₹" + item.pricePerClosure
            jobSkills.text = item.jobSkills
            tvJobSubText.text = "${item.companyLocation} (${item.jobType})"

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, JobReqScreenActivity::class.java)

                intent.putExtra("jobId",item.jobId)
                intent.putExtra("jobRole", item.jobRole)
                intent.putExtra("compName", item.companyName)
                intent.putExtra("compLocation", item.companyLocation)
                intent.putExtra("jobType", item.jobType)
                intent.putExtra("jobSkills", item.jobSkills)
                intent.putExtra("pricePerClosure", item.pricePerClosure)
                intent.putExtra("jobDesc", item.jobDesc)

                context.startActivity(intent)
            }

            btnSubmitResume.setOnClickListener {
                editor.putString("jobId",item.jobId)
                editor.putInt("buttonPosition", position)
                editor.commit()

                val context = itemView.context
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "application/pdf"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                (context as Activity).startActivityForResult(intent, PICK_PDF_REQUEST)
            }

            val selectedFiles = selectedFilesMap[position]

            Log.d("selectedFiles","$selectedFiles")

            if (selectedFiles != null && selectedFiles.isNotEmpty()) {
                btnSubmitResume.background = context.resources.getDrawable(R.drawable.bg_attachresume_disabled)
                tvButtonText.text = "Uploading ${selectedFiles.size} file(s)..."
            } else {
                btnSubmitResume.background = context.resources.getDrawable(R.drawable.bg_attachresume)
                tvButtonText.text = "Submit Resume"
            }
        }
    }


}