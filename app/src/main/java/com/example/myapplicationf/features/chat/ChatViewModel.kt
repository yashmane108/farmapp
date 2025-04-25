package com.example.myapplicationf.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationf.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val context: Map<String, String> = emptyMap() // Store conversation context
)

class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState

    private val keywords = mapOf(
        "weather" to listOf("temperature", "rain", "forecast", "climate"),
        "crop" to listOf("plant", "seed", "harvest", "grow"),
        "pest" to listOf("insect", "disease", "control", "protection"),
        "fertilizer" to listOf("nutrient", "manure", "compost", "soil"),
        "irrigation" to listOf("water", "drip", "sprinkler", "moisture")
    )

    private fun getFallbackResponse(message: String): String {
        // Detect topics in the message
        val detectedTopics = keywords.filter { (topic, words) ->
            words.any { word -> message.contains(word, ignoreCase = true) } ||
            message.contains(topic, ignoreCase = true)
        }.keys.toList()

        return when {
            detectedTopics.contains("weather") ->
                "For weather-related farming decisions:\n" +
                "1. Current conditions are suitable for most farming activities\n" +
                "2. Monitor local weather forecasts for planning\n" +
                "3. Consider crop protection during extreme weather\n\n" +
                "Would you like specific weather-based recommendations?"

            detectedTopics.contains("crop") ->
                "Here are the recommended crops for this season:\n" +
                "1. Kharif (Monsoon): Rice, Maize, Soybean\n" +
                "2. Rabi (Winter): Wheat, Chickpea, Mustard\n" +
                "3. Zaid (Summer): Vegetables, Watermelon\n\n" +
                "Which crop type interests you?"

            detectedTopics.contains("pest") ->
                "Pest management recommendations:\n" +
                "1. Regular crop monitoring\n" +
                "2. Natural pest control methods\n" +
                "3. Integrated Pest Management (IPM)\n\n" +
                "Would you like specific pest control methods?"

            detectedTopics.contains("fertilizer") ->
                "Fertilizer application guide:\n" +
                "1. NPK ratio: 4:2:1 for most crops\n" +
                "2. Organic options: Compost, Vermicompost\n" +
                "3. Application timing: Early morning/evening\n\n" +
                "Need specific fertilizer recommendations?"

            detectedTopics.contains("irrigation") ->
                "Efficient irrigation methods:\n" +
                "1. Drip irrigation (30-50% water saving)\n" +
                "2. Sprinkler system for large fields\n" +
                "3. Traditional flood irrigation\n\n" +
                "Which method would you like to know more about?"

            else -> "I can help you with:\n" +
                "1. Weather-based farming decisions\n" +
                "2. Crop selection and management\n" +
                "3. Pest control methods\n" +
                "4. Fertilizer recommendations\n" +
                "5. Irrigation techniques\n\n" +
                "What would you like to know about?"
        }
    }

    fun updateCurrentMessage(message: String) {
        _chatState.update { it.copy(currentMessage = message) }
    }

    fun sendMessage() {
        val currentMessage = _chatState.value.currentMessage.trim()
        if (currentMessage.isBlank()) return

        viewModelScope.launch {
            // Add user message
            _chatState.update { 
                it.copy(
                    messages = listOf(ChatMessage(currentMessage, true)) + it.messages,
                    currentMessage = "",
                    isLoading = true
                )
            }

            // Simulate AI response
            val response = generateAIResponse(currentMessage)
            _chatState.update {
                it.copy(
                    messages = listOf(ChatMessage(response, false)) + it.messages,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun generateAIResponse(message: String): String {
        // Use the fallback response system for immediate, reliable answers
        return getFallbackResponse(message)
    }
}