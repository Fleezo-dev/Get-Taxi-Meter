package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrandLogo
import com.example.ui.theme.*
import com.example.viewmodel.MeterViewModel

@Composable
fun AuthScreen(viewModel: MeterViewModel, modifier: Modifier = Modifier) {
    val authError by viewModel.authError.collectAsState()
    val loading by viewModel.authLoading.collectAsState()
    var email by remember { mutableStateOf("basheer222@gmail.com") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(AppWhiteBg), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 440.dp).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo(compact = false)
            Spacer(Modifier.height(26.dp))
            Text("Secure Sign In", color = TextPrimary, fontSize = 24.sp)
            Text(
                "Sign in to continue to Get Taxi Meter",
                color = TextSecondary, fontSize = 13.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearAuthError() },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandRed, unfocusedBorderColor = AppCardBorder
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearAuthError() },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = TextSecondary
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandRed, unfocusedBorderColor = AppCardBorder
                ),
                shape = RoundedCornerShape(10.dp)
            )
            if (authError != null) {
                Spacer(Modifier.height(10.dp))
                Text(authError!!, color = BrandRed, fontSize = 12.sp)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { viewModel.signIn(email, password) },
                enabled = !loading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (loading) "SIGNING IN..." else "SIGN IN")
            }
            Spacer(Modifier.height(16.dp))
            Text("Firebase Authentication • Get Taxi Kovai", color = TextMuted, fontSize = 11.sp)
        }
    }
}
