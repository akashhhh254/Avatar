package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvatarCyanPrimary
import com.example.ui.theme.AvatarEmeraldShield
import com.example.ui.viewmodel.AuthMethod
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(AvatarCyanPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isTwoFactorStep) Icons.Default.Key else Icons.Default.Shield,
                        contentDescription = null,
                        tint = AvatarCyanPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val headerTitle = when {
                    uiState.isTwoFactorStep -> "2-Step Verification"
                    uiState.isOtpSent -> "Verify OTP Code"
                    else -> "Welcome to AVATAR"
                }

                val headerSubtitle = when {
                    uiState.isTwoFactorStep -> "Enter your 2-Step Verification PIN to complete authentication"
                    uiState.isOtpSent -> {
                        val target = if (uiState.authMethod == AuthMethod.PHONE) uiState.phoneNumber else uiState.email
                        "Enter the 6-digit OTP code sent to $target"
                    }
                    else -> "Sign in via Phone Number or Email with End-to-End Encryption"
                }

                Text(
                    text = headerTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = headerSubtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                )

                if (!uiState.isOtpSent && !uiState.isTwoFactorStep) {
                    // Auth Method Tabs
                    TabRow(
                        selectedTabIndex = if (uiState.authMethod == AuthMethod.PHONE) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = AvatarCyanPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = uiState.authMethod == AuthMethod.PHONE,
                            onClick = { viewModel.setAuthMethod(AuthMethod.PHONE) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Phone No.", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = uiState.authMethod == AuthMethod.EMAIL,
                            onClick = { viewModel.setAuthMethod(AuthMethod.EMAIL) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Email", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.authMethod == AuthMethod.PHONE) {
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = { viewModel.onPhoneNumberChanged(it) },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            placeholder = { Text("+1 555-0199") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            placeholder = { Text("user@example.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                } else if (uiState.isTwoFactorStep) {
                    // 2-Step Verification PIN Input
                    OutlinedTextField(
                        value = uiState.twoFactorPinInput,
                        onValueChange = { if (it.length <= 6) viewModel.on2FactorPinChanged(it) },
                        label = { Text("2FA Security PIN") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        placeholder = { Text("Enter 4-6 digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // OTP Input
                    OutlinedTextField(
                        value = uiState.otpCode,
                        onValueChange = { if (it.length <= 6) viewModel.onOtpCodeChanged(it) },
                        label = { Text("6-Digit OTP Code") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        placeholder = { Text("123456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { viewModel.requestOtp() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Resend OTP Code", fontSize = 12.sp, color = AvatarCyanPrimary)
                    }
                }

                uiState.successMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = AvatarEmeraldShield,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                uiState.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        when {
                            uiState.isTwoFactorStep -> viewModel.verify2FactorPin()
                            uiState.isOtpSent -> viewModel.verifyOtp()
                            else -> viewModel.requestOtp()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black
                        )
                    } else {
                        val buttonText = when {
                            uiState.isTwoFactorStep -> "Submit 2FA PIN & Unlock"
                            uiState.isOtpSent -> "Verify & Continue"
                            else -> "Send OTP Code"
                        }
                        Text(
                            text = buttonText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield",
                        tint = AvatarEmeraldShield,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Zero-Trust Encrypted Authentication Protocol",
                        fontSize = 11.sp,
                        color = AvatarEmeraldShield
                    )
                }
            }
        }
    }
}

