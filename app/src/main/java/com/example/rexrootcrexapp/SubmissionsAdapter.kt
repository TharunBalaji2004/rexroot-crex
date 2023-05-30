package com.example.rexrootcrexapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubmissionsAdapter(private val resumeList : List<String>) : RecyclerView.Adapter<SubmissionsAdapter.SubmissionsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.submissions_card, parent, false)
        return SubmissionsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubmissionsViewHolder, position: Int) {
        val currentItem = resumeList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return resumeList.size
    }

    inner class SubmissionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resumeName: TextView = itemView.findViewById(R.id.tv_resumename)
        fun bind(item: String){
            resumeName.text = item
        }
    }

}