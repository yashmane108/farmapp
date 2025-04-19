package com.example.myapplicationf.features.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationf.R
import com.example.myapplicationf.ui.theme.HeaderBackground
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationf.auth.AuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("128012155049-15p54mc451mn25ucffb30uk7u3f4vvb1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                
                // Store user data in Firestore
                result.user?.let { user ->
                    val firstName = user.displayName?.split(" ")?.firstOrNull() ?: ""
                    val userData = hashMapOf(
                        "email" to user.email,
                        "firstName" to firstName,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    usersCollection.document(user.uid).set(userData).await()
                }
                
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signUp(email: String, password: String, firstName: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                // Store user data in Firestore with firstName
                result.user?.let { user ->
                    val userData = hashMapOf(
                        "email" to email,
                        "firstName" to firstName,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    usersCollection.document(user.uid).set(userData).await()
                }
                
                // Sign in the user directly after registration
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun handleGoogleSignInError(errorMessage: String) {
        _authState.value = AuthState.Error(errorMessage)
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSuccess
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Password reset failed")
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object RegistrationSuccess : AuthState()
    object ResetPasswordSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showRegistrationSuccess by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var showResetSuccess by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val imageBitmap = remember {
        context.assets.open("farmer img/farmer.jpeg").use { 
            BitmapFactory.decodeStream(it).asImageBitmap() 
        }
    }
    
    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                }
            } catch (e: ApiException) {
                viewModel.handleGoogleSignInError("Google Sign-In failed: ${e.message}")
            }
        }
    }

    // Handle reset password success
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onAuthSuccess()
            is AuthState.RegistrationSuccess -> {
                showRegistrationSuccess = true
                isLogin = true
            }
            is AuthState.ResetPasswordSuccess -> {
                showResetDialog = false
                showResetSuccess = true
            }
            else -> {}
        }
    }

    // Reset Password Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset Password",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF1B5E20)
                    )
                )
            },
            text = {
                Column {
                    Text(
                        "Enter your email address and we'll send you a link to reset your password.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderBackground,
                            focusedLabelColor = HeaderBackground,
                            focusedLeadingIconColor = HeaderBackground
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            viewModel.resetPassword(resetEmail)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HeaderBackground
                    ),
                    enabled = resetEmail.isNotBlank()
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Reset Password")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color(0xFF1B5E20))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Farm Illustration with Card - Now full width
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            color = Color.White
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Farming Illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Content below image with padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLogin) "Welcome Back!" else "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = if (isLogin) "Sign in to continue" else "Sign up to get started",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF2E7D32)
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Google Sign In Button
            OutlinedButton(
                onClick = {
                    val signInClient = viewModel.getGoogleSignInClient(context as Activity)
                    signInClient.signOut().addOnCompleteListener {
                        launcher.launch(signInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color.Black
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray
                )
                Text(
                    text = "  Or  ",
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray
                )
            }

            // First Name field - only shown during sign up
            if (!isLogin) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HeaderBackground,
                        focusedLabelColor = HeaderBackground,
                        focusedLeadingIconColor = HeaderBackground
                    )
                )
            }

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HeaderBackground,
                    focusedLabelColor = HeaderBackground,
                    focusedLeadingIconColor = HeaderBackground
                )
            )

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HeaderBackground,
                    focusedLabelColor = HeaderBackground
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = if (passwordVisible) HeaderBackground else Color.Gray
                        )
                    }
                }
            )

            // Forgot Password Button (only shown during login)
            if (isLogin) {
                TextButton(
                    onClick = { 
                        showResetDialog = true
                        resetEmail = email  // Pre-fill with email if entered
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        "Forgot Password?",
                        color = HeaderBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Login/Sign Up Button
            Button(
                onClick = {
                    if (isLogin) {
                        viewModel.signIn(email, password)
                    } else {
                        viewModel.signUp(email, password, firstName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HeaderBackground
                ),
                enabled = if (isLogin) email.isNotBlank() && password.isNotBlank() 
                         else email.isNotBlank() && password.isNotBlank() && firstName.isNotBlank()
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        if (isLogin) "Login" else "Sign Up",
                        fontSize = 16.sp
                    )
                }
            }

            // Switch between Login/Sign Up
            TextButton(
                onClick = { 
                    isLogin = !isLogin
                    if (isLogin) firstName = ""
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    if (isLogin) "Don't have an account? Sign Up" 
                    else "Already have an account? Login",
                    color = HeaderBackground
                )
            }

            // Error Message
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            // Registration Success Message
            if (showRegistrationSuccess) {
                Text(
                    text = "Registration successful! Please login with your credentials.",
                    color = HeaderBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Reset Password Success Message
            if (showResetSuccess) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Password reset link sent to your email!",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
} 