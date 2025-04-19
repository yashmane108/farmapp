package com.example.myapplicationf.features.about

import androidx.lifecycle.ViewModel

class AboutUsViewModel : ViewModel() {
    val appDescription = "Our project aims to revolutionize farming through technology, providing smart solutions for modern agriculture."
    
    val teamMembers = listOf(
        "Ganesh Nikam",
        "Yash Mane",
        "Kunal Chavan",
        "Sakshi Sawant",
        "demo001"
    )
    
    val contactNumbers = listOf(
        "+91 7083890547",
        "+91 7218651320"
    )
    
    val socialMedia = listOf(
        "@ai.farmapp67"
    )
}
