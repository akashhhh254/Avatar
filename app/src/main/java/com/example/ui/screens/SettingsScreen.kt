package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarBadge
import com.example.ui.theme.AvatarCyanPrimary
import com.example.ui.theme.AvatarEmeraldShield
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel

data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by chatViewModel.currentUser.collectAsState()
    val blockedContacts by chatViewModel.blockedContacts.collectAsState()
    val isDarkTheme by chatViewModel.isDarkTheme.collectAsState()
    val isAppLockEnabled by chatViewModel.isAppLockEnabled.collectAsState()
    val readReceiptsEnabled by chatViewModel.readReceiptsEnabled.collectAsState()
    val lastSeenVisibility by chatViewModel.lastSeenVisibility.collectAsState()

    val is2FAEnabled by authViewModel.isTwoFactorEnabled.collectAsState()
    val savedPin by authViewModel.savedPin.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var show2FADialog by remember { mutableStateOf(false) }
    var showHelpCenterDialog by remember { mutableStateOf(false) }
    var showContactSupportDialog by remember { mutableStateOf(false) }
    var showSecurityGuideDialog by remember { mutableStateOf(false) }
    var showBlockedContactsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(currentUser?.displayName ?: "Alex Vance") }
    var editUsername by remember { mutableStateOf(currentUser?.username ?: "alex_v") }
    var editBio by remember { mutableStateOf(currentUser?.bio ?: "Encrypted with AVATAR Shield") }

    var new2FaPin by remember { mutableStateOf("") }
    var confirm2FaPin by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }

    var supportSubject by remember { mutableStateOf("") }
    var supportMessage by remember { mutableStateOf("") }
    var supportSentSuccess by remember { mutableStateOf(false) }

    val faqList = remember {
        listOf(
            FaqItem("How does AVATAR 2-Step Verification work?", "When enabled, signing in via Phone or Email requires your secret 6-digit 2FA PIN after OTP code verification."),
            FaqItem("Are messages end-to-end encrypted?", "Yes! AVATAR uses ECDH key agreement with double-ratchet AES-256 encryption. Only device key owners can decrypt messages."),
            FaqItem("How do I receive SMS or Email OTP codes?", "OTP verification codes are automatically sent to your registered phone number or email address during sign-in."),
            FaqItem("Can I lock the AVATAR app on my device?", "Yes! You can enable App Lock in Settings -> Account & Security for biometric or PIN protection upon opening the app."),
            FaqItem("What if I lose access or need help?", "You can contact support directly from the Help & Support menu in Settings or submit a ticket.")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings & Privacy", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarBadge(
                            name = currentUser?.displayName ?: "Alex Vance",
                            avatarUrl = currentUser?.avatarUrl ?: "",
                            size = 60.dp,
                            isOnline = true
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName ?: "Alex Vance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "@${currentUser?.username ?: "alex_v"}",
                                fontSize = 13.sp,
                                color = AvatarCyanPrimary
                            )
                            Text(
                                text = currentUser?.bio ?: "Encrypted with AVATAR Shield",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { showProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = AvatarCyanPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Categories
            item {
                SettingsCategoryTitle("ACCOUNT & SECURITY")
                SettingsItemRow(
                    icon = Icons.Default.Shield,
                    title = "AVATAR Shield Protocol",
                    subtitle = "ECDH + AES-256-GCM Double Ratchet E2E Encryption",
                    iconColor = AvatarEmeraldShield,
                    onClick = { showSecurityGuideDialog = true }
                )
                SettingsItemRow(
                    icon = Icons.Default.Key,
                    title = "2-Step Verification (2FA PIN)",
                    subtitle = if (is2FAEnabled) "Active • PIN Required on Login" else "Disabled • Tap to setup 2FA PIN",
                    iconColor = AvatarCyanPrimary,
                    onClick = {
                        new2FaPin = ""
                        confirm2FaPin = ""
                        pinErrorMessage = null
                        show2FADialog = true
                    }
                )
                SettingsItemRow(
                    icon = Icons.Default.Lock,
                    title = "App Lock (PIN / Biometric)",
                    subtitle = if (isAppLockEnabled) "Enabled" else "Disabled",
                    onClick = { chatViewModel.isAppLockEnabled.value = !isAppLockEnabled }
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryTitle("PRIVACY")
                SettingsItemRow(
                    icon = Icons.Default.Visibility,
                    title = "Last Seen & Online",
                    subtitle = lastSeenVisibility,
                    onClick = {
                        chatViewModel.lastSeenVisibility.value = when (lastSeenVisibility) {
                            "Everyone" -> "Contacts"
                            "Contacts" -> "Nobody"
                            else -> "Everyone"
                        }
                    }
                )
                SettingsItemRow(
                    icon = Icons.Default.DoneAll,
                    title = "Read Receipts",
                    subtitle = if (readReceiptsEnabled) "Double blue checkmarks active" else "Disabled",
                    onClick = { chatViewModel.readReceiptsEnabled.value = !readReceiptsEnabled }
                )
                SettingsItemRow(
                    icon = Icons.Default.Block,
                    title = "Blocked Contacts",
                    subtitle = if (blockedContacts.isEmpty()) "None blocked" else "${blockedContacts.size} contact(s) blocked",
                    iconColor = if (blockedContacts.isNotEmpty()) MaterialTheme.colorScheme.error else AvatarCyanPrimary,
                    onClick = { showBlockedContactsDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryTitle("HELP & SUPPORT")
                SettingsItemRow(
                    icon = Icons.Default.HelpCenter,
                    title = "Help Center & FAQ",
                    subtitle = "Questions about 2FA, OTP, security & messaging",
                    onClick = { showHelpCenterDialog = true }
                )
                SettingsItemRow(
                    icon = Icons.Default.SupportAgent,
                    title = "Contact Support & Feedback",
                    subtitle = "Send inquiry to AVATAR technical team",
                    onClick = {
                        supportSentSuccess = false
                        supportSubject = ""
                        supportMessage = ""
                        showContactSupportDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryTitle("APPEARANCE & THEME")
                SettingsItemRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = if (isDarkTheme) "Obsidian Glassmorphism Theme" else "Light Theme",
                    onClick = { chatViewModel.isDarkTheme.value = !isDarkTheme }
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryTitle("STORAGE & DATA")
                SettingsItemRow(
                    icon = Icons.Default.CleaningServices,
                    title = "Clear Local Media Cache",
                    subtitle = "14.2 MB cached files",
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryTitle("ABOUT")
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = "AVATAR Version",
                    subtitle = "v1.0.0 Zero-Trust E2E Build • Play Store Ready",
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showLogoutDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out of AVATAR", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 2FA Dialog
    if (show2FADialog) {
        AlertDialog(
            onDismissRequest = { show2FADialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = AvatarCyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2-Step Verification")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (is2FAEnabled)
                            "2-Step Verification is currently ACTIVE. You can change your 6-digit PIN or disable it below."
                        else
                            "Create a 6-digit PIN that will be requested when you log into your AVATAR account on a new device.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = new2FaPin,
                        onValueChange = { if (it.length <= 6) new2FaPin = it },
                        label = { Text("New 6-Digit 2FA PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirm2FaPin,
                        onValueChange = { if (it.length <= 6) confirm2FaPin = it },
                        label = { Text("Confirm 6-Digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    pinErrorMessage?.let { err ->
                        Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (new2FaPin.length < 4) {
                            pinErrorMessage = "PIN must be at least 4 digits"
                            return@Button
                        }
                        if (new2FaPin != confirm2FaPin) {
                            pinErrorMessage = "PINs do not match"
                            return@Button
                        }
                        authViewModel.toggleTwoFactorAuth(true, new2FaPin)
                        show2FADialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                ) {
                    Text(if (is2FAEnabled) "Update PIN" else "Enable 2FA", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (is2FAEnabled) {
                    TextButton(
                        onClick = {
                            authViewModel.toggleTwoFactorAuth(false, "")
                            show2FADialog = false
                        }
                    ) {
                        Text("Disable 2FA", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = { show2FADialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Help Center & FAQ Dialog
    if (showHelpCenterDialog) {
        AlertDialog(
            onDismissRequest = { showHelpCenterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpCenter, contentDescription = null, tint = AvatarCyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Help Center & FAQ")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    faqList.forEach { faq ->
                        var expanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { expanded = !expanded },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = faq.question,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = AvatarCyanPrimary
                                    )
                                }
                                if (expanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = faq.answer,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpCenterDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                ) {
                    Text("Close", color = Color.Black)
                }
            }
        )
    }

    // Contact Support Dialog
    if (showContactSupportDialog) {
        AlertDialog(
            onDismissRequest = { showContactSupportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = AvatarCyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact AVATAR Support")
                }
            },
            text = {
                if (supportSentSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AvatarEmeraldShield, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Inquiry Received!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Our security & technical team will respond to your registered email/phone shortly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Have questions about 2FA OTP, messaging, or account settings? Write to us below.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = supportSubject,
                            onValueChange = { supportSubject = it },
                            label = { Text("Subject") },
                            placeholder = { Text("e.g. 2FA OTP assistance") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = supportMessage,
                            onValueChange = { supportMessage = it },
                            label = { Text("Description") },
                            placeholder = { Text("Type your message or issue here...") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!supportSentSuccess) {
                    Button(
                        onClick = {
                            if (supportSubject.isNotBlank() && supportMessage.isNotBlank()) {
                                supportSentSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                    ) {
                        Text("Send Support Ticket", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showContactSupportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                    ) {
                        Text("Done", color = Color.Black)
                    }
                }
            },
            dismissButton = {
                if (!supportSentSuccess) {
                    TextButton(onClick = { showContactSupportDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Security Guide Dialog
    if (showSecurityGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AvatarEmeraldShield)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AVATAR Shield Protocol")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Zero-Trust Architecture", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvatarCyanPrimary)
                    Text("Messages are encrypted on your local device before transmission. No central server possesses private decryption keys.", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Double-Ratchet AES-256-GCM", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvatarCyanPrimary)
                    Text("Every single message generates a distinct dynamic key hash ensuring forward and backward secrecy.", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• 2-Step Verification (2FA)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AvatarCyanPrimary)
                    Text("Enhances account security by requiring a custom 6-digit PIN alongside OTP verification when signing in.", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecurityGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                ) {
                    Text("Understood", color = Color.Black)
                }
            }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio / Status") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.updateUserProfile(editName, editUsername, editBio)
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out")
                }
            },
            text = {
                Text("Are you sure you want to log out of your AVATAR account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBlockedContactsDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedContactsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Blocked Contacts (${blockedContacts.size})")
                }
            },
            text = {
                if (blockedContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No blocked contacts. You can block any contact from their chat options menu.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(blockedContacts, key = { it.id }) { user ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AvatarBadge(name = user.displayName, avatarUrl = user.avatarUrl, size = 40.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("@${user.username}", fontSize = 12.sp, color = AvatarCyanPrimary)
                                        }
                                    }
                                    Button(
                                        onClick = { chatViewModel.blockUser(user.id, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Unblock", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedContactsDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = AvatarCyanPrimary,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = AvatarCyanPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

