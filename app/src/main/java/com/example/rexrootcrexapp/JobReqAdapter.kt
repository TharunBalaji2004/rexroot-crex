package com.example.rexrootcrexapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rexrootcrexapp.Data.JobReqDataClass

class JobReqAdapter(private val jobReqList : ArrayList<JobReqDataClass>) : RecyclerView.Adapter<JobReqAdapter.JobReqViewHolder>() {

    class JobReqViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val jobRole : TextView = itemView.findViewById(R.id.tv_jobrole)
        val compName : TextView = itemView.findViewById(R.id.tv_compname)
        val pricePerClosure : TextView = itemView.findViewById(R.id.tv_priceperclosure)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobReqAdapter.JobReqViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sample_jobreqcard,parent,false)
        return JobReqViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobReqAdapter.JobReqViewHolder, position: Int) {
        holder.jobRole.text = jobReqList[position].jobRole
        holder.compName.text = jobReqList[position].companyName
        holder.pricePerClosure.text = jobReqList[position].pricePerClosure

        holder.itemView.setOnClickListener {

            val context = holder.itemView.context
            val intent = Intent(context, JobReqScreenActivity::class.java)

            // Data passing
            intent.putExtra("jobRole",jobReqList[position].jobRole)
            intent.putExtra("compName",jobReqList[position].companyName)
            intent.putExtra("pricePerClosure",jobReqList[position].pricePerClosure)
            intent.putExtra("jobDesc",jobReqList[position].jobDesc)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return jobReqList.size
    }

}