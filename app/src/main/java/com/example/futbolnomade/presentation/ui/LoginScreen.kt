package com.example.futbolnomade.presentation.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.presentation.viewModel.AuthResult
import com.example.futbolnomade.presentation.viewModel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,           // ← siempre recibido desde AppNavigation, sin default
    onLoginSuccess: (nombre: String, email: String) -> Unit,
    onSignUpClick: () -> Unit = {}
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError    by remember { mutableStateOf<String?>(null) }

    val darkBackground = Color(0xFF171516)
    val fieldGray      = Color(0xFF7A7A7A)
    val neonGreen      = Color(0xFF00FF7F)

    fun validarCampos(): Boolean {
        emailError    = null
        passwordError = null
        loginError    = null

        if (email.trim().isEmpty())  { emailError = "Ingresá tu email"; return false }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { emailError = "Email inválido"; return false }
        if (password.trim().isEmpty()) { passwordError = "Ingresá tu contraseña"; return false }
        if (password.trim().length < 6) { passwordError = "La contraseña debe tener al menos 6 caracteres"; return false }

        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))

        Text("Ingresar", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(72.dp))

        Text("EMAIL", color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null; loginError = null },
            placeholder = { Text("hello@reallygreatsite.com", color = Color.White.copy(alpha = 0.75f)) },
            isError = emailError != null || loginError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = fieldGray,
                unfocusedContainerColor = fieldGray,
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                errorBorderColor        = Color.Red,
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White,
                cursorColor             = neonGreen
            )
        )
        emailError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(Modifier.height(14.dp))

        Text("PASSWORD", color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null; loginError = null },
            placeholder = { Text("••••••", color = Color.White.copy(alpha = 0.75f)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null || loginError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = fieldGray,
                unfocusedContainerColor = fieldGray,
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                errorBorderColor        = Color.Red,
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White,
                cursorColor             = neonGreen
            )
        )
        passwordError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }
        loginError?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(Modifier.height(36.dp))

        OutlinedButton(
            onClick = {
                if (!validarCampos()) return@OutlinedButton

                authViewModel.login(email.trim(), password.trim()) { result ->
                    when (result) {
                        is AuthResult.Success -> {
                            // Pasar nombre real del usuario (no el email) al Home
                            val usuario = authViewModel.usuarioActual
                            onLoginSuccess(usuario?.nombre ?: email.trim(), email.trim())
                        }
                        is AuthResult.Error -> loginError = result.mensaje
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp, brush = SolidColor(neonGreen)
            )
        ) { Text("Log in") }

        Spacer(Modifier.height(18.dp))

        Text("No tenés cuenta? Crea una aquí", color = Color.White, fontSize = 11.sp)

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp, brush = SolidColor(neonGreen)
            )
        ) { Text("Sign up") }
    }
}