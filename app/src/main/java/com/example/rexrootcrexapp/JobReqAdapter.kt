package com.example.rexrootcrexapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rexrootcrexapp.Data.JobReqDataClass
import java.util.Locale

class JobReqAdapter(private val jobReqList : ArrayList<JobReqDataClass>) : RecyclerView.Adapter<JobReqAdapter.JobReqViewHolder>(), Filterable {

    private var filteredList: List<JobReqDataClass> = jobReqList

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

    inner class JobReqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobRole: TextView = itemView.findViewById(R.id.tv_jobrole)
        private val compName: TextView = itemView.findViewById(R.id.tv_compname)
        private val pricePerClosure: TextView = itemView.findViewById(R.id.tv_priceperclosure)

        fun bind(item: JobReqDataClass) {
            jobRole.text = item.jobRole
            compName.text = item.companyName
            pricePerClosure.text = "₹" + item.pricePerClosure

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, JobReqScreenActivity::class.java)

                intent.putExtra("jobId",item.jobId)
                intent.putExtra("jobRole", item.jobRole)
                intent.putExtra("compName", item.companyName)
                intent.putExtra("pricePerClosure", item.pricePerClosure)
                intent.putExtra("jobDesc", item.jobDesc)

                context.startActivity(intent)
            }
        }
    }

}