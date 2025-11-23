package week11.st185898.finalproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import week11.st185898.finalproject.ui.auth.AuthScreen
import week11.st185898.finalproject.ui.home.HomeScreen
import week11.st185898.finalproject.ui.home.HomeTab

@Composable
fun SmartCampusApp(viewModel: MainViewModel) {
    val authState by viewModel.authState.collectAsState()
    val snackbarMessage by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SmartCampusColors.BackgroundGradientTop,
                            SmartCampusColors.BackgroundGradientMid,
                            SmartCampusColors.BackgroundGradientBottom
                        )
                    )
                )
        ) {
            when (authState) {
                is AuthState.SignedOut,
                is AuthState.Error,
                AuthState.Idle -> AuthScreen(
                    isLoading = authState is AuthState.Loading,
                    authState = authState,
                    onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                    onSignUp = { email, pass -> viewModel.signUp(email, pass) }
                )

                is AuthState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }

                is AuthState.Authenticated -> HomeScreen(viewModel)
            }
        }
    }
}

