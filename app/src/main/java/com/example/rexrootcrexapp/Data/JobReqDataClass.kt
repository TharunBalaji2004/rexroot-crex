package com.example.rexrootcrexapp.Data

data class JobReqDataClass(
    val jobId: String? = "N/A",
    val jobRole : String? = "N/A",
    val companyName : String? = "N/A",
    val companyLocation : String? = "N/A",
    val jobType : String? = "N/A",
    val jobSkills : String? = "N/A",
    val pricePerClosure : String? = "N/A",
    val jobDesc : String? = "N/A",
    val submitdata: HashMap<String,Any>?= hashMapOf<String,Any>()
)
