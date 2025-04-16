package com.example.myapplicationf.features.marketplace

class LocationService {
    // Comprehensive list of villages in Satara district
    private val villages = listOf(
        // A
        "Adarki", "Adavali", "Ambavade", "Ambe", "Ambegaon", "Anewadi", "Apshinge", "Atit", "Aundh",
        
        // B
        "Bavdhan", "Bhuinj", "Bhose", "Borgaon", "Budukh", "Bhilawadi", "Bopegaon", "Borgaon",
        
        // C
        "Chafal", "Chandoli", "Chinchner", "Chikhali", "Chounde",
        
        // D
        "Dahiwadi", "Degaon", "Devrashtre", "Dhawadshi", "Dhom", "Dive", "Durgalwadi",
        
        // G
        "Gadhe", "Gargoti", "Ghigewadi", "Gondawale", "Gudhe", "Gurholi",
        
        // H
        "Helwak", "Hingangaon", "Hivare",
        
        // J
        "Jakatwadi", "Jambhulni", "Jawali", "Jejuri",
        
        // K
        "Kadegaon", "Kalamb", "Karad", "Karandi", "Karandoli", "Kasegaon", "Katapur", "Khed", "Kinhai", "Koregaon", "Kumthe",
        
        // L
        "Lonand", "Limb", "Lohare",
        
        // M
        "Mahabaleshwar", "Mahuli", "Malkapur", "Malwadi", "Mandave", "Mangalwedha", "Mardi", "Masur", "Medha", "Metgutad",
        
        // N
        "Nagthane", "Nandgaon", "Natepute", "Nerle", "Nigdi",
        
        // O
        "Ogalewadi", "Ozarde",
        
        // P
        "Pachgani", "Padali", "Pali", "Pandharpur", "Pangari", "Parali", "Pargaon", "Patan", "Patgaon", "Pusegaon",
        
        // R
        "Rahimatpur", "Rajapur", "Randullabad", "Rethare", "Rohit",
        
        // S
        "Sajjangad", "Satara City", "Shendre", "Shirgaon", "Shivthar", "Solashi", "Sonawade", "Supane",
        
        // T
        "Takali", "Tambave", "Tambe", "Tasgaon", "Thoseghar",
        
        // U
        "Umbraj", "Uran", "Urmodi",
        
        // V
        "Vaduj", "Vangal", "Vanvasmachi", "Varye", "Vele", "Velvand", "Vita",
        
        // W
        "Wadgaon", "Wagholi", "Walva", "Wathar", "Wategaon", "Wai",
        
        // Y
        "Yadavwadi", "Yawat", "Yekambe"
    ).sorted() // Keep the list alphabetically sorted

    fun getSuggestions(query: String): List<String> {
        // Return empty list only if query is completely empty
        if (query.isEmpty()) return emptyList()
        
        // First get exact matches that start with the query
        val startsWithMatches = villages.filter { 
            it.lowercase().startsWith(query.lowercase())
        }
        
        // Then get other matches that contain the query but don't start with it
        val containsMatches = villages.filter { 
            it.lowercase().contains(query.lowercase()) && !it.lowercase().startsWith(query.lowercase())
        }
        
        // Return starts-with matches first, then contains matches
        // Limit total suggestions to 10 to avoid overwhelming the UI
        return (startsWithMatches + containsMatches).take(10)
    }

    suspend fun getLocationSuggestions(query: String): List<String> {
        // Simulated location suggestions
        return when {
            query.length >= 2 -> listOf(
                "$query, Maharashtra",
                "$query City",
                "$query District"
            )
            else -> emptyList()
        }
    }
}
