package com.example.myapplicationf.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val content: String,
    val isUser: Boolean
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false
)

class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState

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

    private fun generateAIResponse(message: String): String {
        // In a real app, this would call an AI API
        return when {
            message.contains("weather", ignoreCase = true) -> 
                "Based on current forecasts, the weather conditions are suitable for farming activities. Would you like specific details about temperature or rainfall?"
            
            message.contains("pest", ignoreCase = true) -> 
                "For pest control, I recommend implementing Integrated Pest Management (IPM). Would you like specific recommendations for your crops?"
            
            message.contains("fertilizer", ignoreCase = true) -> 
                "The best fertilizer depends on your soil type and crop. Have you done a recent soil test? I can provide specific recommendations based on your soil analysis."
            
            message.contains("crop", ignoreCase = true) -> 
                "Based on the current season and your location, I can suggest several suitable crops. Would you like to know more about specific crop options?"
            
            else -> "I can help you with farming-related questions about weather, pests, fertilizers, and crop selection. What specific information do you need?"
        }
    }
}
