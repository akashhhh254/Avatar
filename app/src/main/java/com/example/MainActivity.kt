package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.AvatarTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val chatViewModel: ChatViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()

            val isDarkTheme by chatViewModel.isDarkTheme.collectAsState()
            val activeCallState by chatViewModel.activeCallState.collectAsState()
            val authUiState by authViewModel.uiState.collectAsState()

            AvatarTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    if (activeCallState.isCallActive) {
                        CallScreen(
                            callState = activeCallState,
                            onMuteToggle = { chatViewModel.toggleMuteCall() },
                            onSpeakerToggle = { chatViewModel.toggleSpeakerCall() },
                            onEndCall = { chatViewModel.endCall() }
                        )
                    } else {
                        val startDestination = "splash"

                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("splash") {
                                SplashScreen(
                                    onSplashFinished = {
                                        val dest = if (authUiState.isLoggedIn) "home" else "auth"
                                        navController.navigate(dest) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("auth") {
                                AuthScreen(
                                    viewModel = authViewModel,
                                    onAuthSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("home") {
                                MainHomeScreen(
                                    viewModel = chatViewModel,
                                    onOpenConversation = { convId ->
                                        chatViewModel.selectConversation(convId)
                                        navController.navigate("chat/$convId")
                                    },
                                    onOpenNewChatGroup = {
                                        navController.navigate("new_chat")
                                    },
                                    onOpenSettings = {
                                        navController.navigate("settings")
                                    },
                                    onLogout = {
                                        authViewModel.logout()
                                        navController.navigate("auth") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    onInitiateCall = { id, name, avatar, type ->
                                        chatViewModel.initiateCall(id, name, avatar, type)
                                    }
                                )
                            }

                            composable(
                                route = "chat/{conversationId}",
                                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
                                LaunchedEffect(convId) {
                                    chatViewModel.selectConversation(convId)
                                }

                                ConversationScreen(
                                    viewModel = chatViewModel,
                                    onBackClick = { navController.popBackStack() },
                                    onInitiateCall = { id, name, avatar, type ->
                                        chatViewModel.initiateCall(id, name, avatar, type)
                                    },
                                    onOpenGroupInfo = { groupId ->
                                        // Open group info modal or screen
                                    }
                                )
                            }

                            composable("new_chat") {
                                NewChatGroupScreen(
                                    viewModel = chatViewModel,
                                    onBackClick = { navController.popBackStack() },
                                    onChatCreated = { convId ->
                                        chatViewModel.selectConversation(convId)
                                        navController.navigate("chat/$convId") {
                                            popUpTo("home")
                                        }
                                    }
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    chatViewModel = chatViewModel,
                                    authViewModel = authViewModel,
                                    onBackClick = { navController.popBackStack() },
                                    onLogout = {
                                        navController.navigate("auth") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
