package week11.st185898.finalproject.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st185898.finalproject.ui.AuthState
import week11.st185898.finalproject.ui.SmartCampusColors

@Composable
fun AuthScreen(
    isLoading: Boolean,
    authState: AuthState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Campus Assistant",
            color = SmartCampusColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SmartCampusColors.Card
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    text = if (isLoginMode) "Sign In" else "Create Account",
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                AuthTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it }
                )

                AuthTextField(
                    label = "Password",
                    value = pass,
                    onValueChange = { pass = it },
                    isPassword = true
                )

                if (isLoginMode) {
                    Text(
                        text = "Forgot Password?",
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Button(
                    onClick = {
                        if (isLoginMode) onSignIn(email, pass)
                        else onSignUp(email, pass)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SmartCampusColors.AccentGreen
                    )
                ) {
                    Text(
                        text = if (isLoginMode) "Sign In" else "Sign Up",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isLoginMode)
                            "Don't have an account? "
                        else
                            "Already have an account? ",
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = { isLoginMode = !isLoginMode }) {
                        Text(
                            text = if (isLoginMode) "Sign up" else "Sign In",
                            color = SmartCampusColors.AccentCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = authState.message,
                color = SmartCampusColors.DangerRed,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = SmartCampusColors.AccentCyan,
            focusedBorderColor = SmartCampusColors.AccentGreen,
            unfocusedLabelColor = SmartCampusColors.AccentCyan,
            focusedLabelColor = SmartCampusColors.AccentGreen,
            cursorColor = Color.White
        ),
        visualTransformation = if (isPassword)
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        else
            androidx.compose.ui.text.input.VisualTransformation.None
    )
}

