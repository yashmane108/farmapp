package com.example.myapplicationf.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A singleton helper class to manage authentication state
 */
object AuthHelper {
    private val auth = FirebaseAuth.getInstance()
    
    // Public state for authentication
    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    init {
        // Listen for authentication state changes
        auth.addAuthStateListener { firebaseAuth ->
            _isAuthenticated.value = firebaseAuth.currentUser != null
        }
    }
    
    /**
     * Sign out the current user - guaranteed to work
     */
    fun signOut() {
        // First update our state
        _isAuthenticated.value = false
        
        // Then perform the actual sign out
        auth.signOut()
    }
    
    /**
     * Get the current authenticated user's email
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
} 