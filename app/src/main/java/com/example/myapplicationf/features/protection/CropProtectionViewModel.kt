package com.example.myapplicationf.features.protection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ThreatSeverity {
    HIGH, MEDIUM, LOW
}

data class CropThreat(
    val name: String,
    val description: String,
    val severity: ThreatSeverity,
    val detectedDate: String
)

data class ProtectionRecord(
    val action: String,
    val date: String,
    val status: String
)

data class CropProtectionInfo(
    val cropName: String,
    val commonIssues: List<String>,
    val preventionMethods: List<String>,
    val treatment: String
)

data class ProtectionTip(
    val category: String,
    val title: String,
    val description: String
)

data class ProtectionState(
    val threats: List<CropThreat> = emptyList(),
    val history: List<ProtectionRecord> = emptyList(),
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<CropProtectionInfo> = emptyList(),
    val commonTips: List<ProtectionTip> = emptyList()
)

class CropProtectionViewModel : ViewModel() {
    private val _protectionState = MutableStateFlow(ProtectionState())
    val protectionState: StateFlow<ProtectionState> = _protectionState

    private val cropDatabase = mapOf(
        "rice" to CropProtectionInfo(
            cropName = "Rice",
            commonIssues = listOf(
                "Rice Blast Disease",
                "Bacterial Leaf Blight",
                "Brown Spot",
                "Stem Rot"
            ),
            preventionMethods = listOf(
                "Use disease-resistant varieties",
                "Maintain proper water management",
                "Practice crop rotation",
                "Apply balanced fertilization"
            ),
            treatment = "For blast disease, apply fungicides containing tricyclazole. For bacterial blight, avoid excess nitrogen and maintain good drainage."
        ),
        "wheat" to CropProtectionInfo(
            cropName = "Wheat",
            commonIssues = listOf(
                "Rust Disease",
                "Powdery Mildew",
                "Loose Smut",
                "Root Rot"
            ),
            preventionMethods = listOf(
                "Plant certified seed",
                "Use resistant varieties",
                "Follow proper spacing",
                "Monitor soil moisture"
            ),
            treatment = "Apply propiconazole-based fungicides for rust and powdery mildew. For smut, use systemic fungicide seed treatment."
        ),
        "tomato" to CropProtectionInfo(
            cropName = "Tomato",
            commonIssues = listOf(
                "Early Blight",
                "Late Blight",
                "Fusarium Wilt",
                "Leaf Spot"
            ),
            preventionMethods = listOf(
                "Maintain proper spacing",
                "Use drip irrigation",
                "Remove infected leaves",
                "Practice mulching"
            ),
            treatment = "Apply copper-based fungicides for blights. For fusarium wilt, soil solarization and crop rotation are recommended."
        ),
        "potato" to CropProtectionInfo(
            cropName = "Potato",
            commonIssues = listOf(
                "Late Blight",
                "Early Blight",
                "Black Scurf",
                "Common Scab"
            ),
            preventionMethods = listOf(
                "Use certified seed potatoes",
                "Practice crop rotation",
                "Maintain proper soil pH",
                "Avoid overwatering"
            ),
            treatment = "For blights, apply protective fungicides. For scab, maintain soil pH below 5.5 and ensure good drainage."
        ),
        "cotton" to CropProtectionInfo(
            cropName = "Cotton",
            commonIssues = listOf(
                "Bollworms",
                "Cotton Leaf Curl",
                "Bacterial Blight",
                "Root Rot"
            ),
            preventionMethods = listOf(
                "Use Bt cotton varieties",
                "Monitor pest populations",
                "Maintain field sanitation",
                "Follow proper spacing"
            ),
            treatment = "Implement IPM strategies for bollworms. For leaf curl, remove infected plants and control whitefly vectors."
        )
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val threats = listOf(
            CropThreat(
                "Leaf Blight",
                "Fungal infection detected in wheat crops",
                ThreatSeverity.HIGH,
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            ),
            CropThreat(
                "Aphids",
                "Pest infestation in tomato plants",
                ThreatSeverity.MEDIUM,
                LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            ),
            CropThreat(
                "Nutrient Deficiency",
                "Iron deficiency in rice crops",
                ThreatSeverity.LOW,
                LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_DATE)
            )
        )

        val history = listOf(
            ProtectionRecord(
                "Pesticide Application",
                LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE),
                "Completed"
            ),
            ProtectionRecord(
                "Disease Treatment",
                LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_DATE),
                "Completed"
            ),
            ProtectionRecord(
                "Soil Treatment",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                "In Progress"
            )
        )

        val commonTips = listOf(
            ProtectionTip(
                category = "Pest",
                title = "Natural Pest Control",
                description = "Use companion planting and beneficial insects to control pests naturally"
            ),
            ProtectionTip(
                category = "Disease",
                title = "Disease Prevention",
                description = "Maintain proper spacing between plants to improve air circulation"
            ),
            ProtectionTip(
                category = "Soil",
                title = "Soil Health",
                description = "Regular soil testing helps prevent nutrient deficiencies"
            ),
            ProtectionTip(
                category = "Disease",
                title = "Crop Rotation",
                description = "Rotate crops annually to prevent soil-borne diseases"
            ),
            ProtectionTip(
                category = "Pest",
                title = "Integrated Pest Management",
                description = "Monitor pest populations regularly and use threshold-based interventions"
            )
        )

        _protectionState.value = ProtectionState(
            threats = threats,
            history = history,
            commonTips = commonTips
        )
    }

    fun updateSearchQuery(query: String) {
        _protectionState.value = _protectionState.value.copy(searchQuery = query)
        if (query.isNotEmpty()) {
            searchCrop()
        }
    }

    fun searchCrop() {
        val query = _protectionState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            _protectionState.value = _protectionState.value.copy(searchResults = emptyList())
            return
        }

        val results = cropDatabase.filter { (key, _) ->
            key.contains(query)
        }.values.toList()

        _protectionState.value = _protectionState.value.copy(searchResults = results)
    }

    fun scanForDisease() {
        viewModelScope.launch {
            _protectionState.value = _protectionState.value.copy(isScanning = true)
            
            // Simulate scanning process
            delay(2000)

            // Add a new threat if disease detected (simulated)
            val newThreat = CropThreat(
                "Powdery Mildew",
                "Early signs detected in cucumber plants",
                ThreatSeverity.MEDIUM,
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            )

            val currentThreats = _protectionState.value.threats.toMutableList()
            currentThreats.add(0, newThreat)

            _protectionState.value = _protectionState.value.copy(
                threats = currentThreats,
                isScanning = false
            )
        }
    }
}
