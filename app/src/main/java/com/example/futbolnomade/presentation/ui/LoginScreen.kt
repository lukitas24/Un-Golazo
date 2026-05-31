package com.example.futbolnomade.presentation.ui

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onSignUpClick: () -> Unit = {}
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val darkBackground = Color(0xFF171516)
    val fieldGray = Color(0xFF7A7A7A)
    val neonGreen = Color(0xFF00FF7F)

    fun validarLogin(): Boolean {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        emailError = null
        passwordError = null

        if (cleanEmail.isEmpty()) {
            emailError = "Ingresá tu email"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            emailError = "Email inválido"
            return false
        }

        if (cleanPassword.isEmpty()) {
            passwordError = "Ingresá tu contraseña"
            return false
        }

        if (cleanPassword.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        Text(
            text = "Ingresar",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(72.dp))

        Text(
            text = "EMAIL",
            color = neonGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            placeholder = { Text("hello@reallygreatsite.com", color = Color.White.copy(alpha = 0.75f)) },
            isError = emailError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldGray,
                unfocusedContainerColor = fieldGray,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Red,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = neonGreen
            )
        )

        emailError?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PASSWORD",
            color = neonGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            placeholder = { Text("••••••", color = Color.White.copy(alpha = 0.75f)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldGray,
                unfocusedContainerColor = fieldGray,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Red,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = neonGreen
            )
        )

        passwordError?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedButton(
            onClick = {
                if (!validarLogin()) return@OutlinedButton

                val cleanEmail = email.trim()
                val cleanPassword = password.trim()

                if (cleanEmail == "admin@gmail.com" && cleanPassword == "123456") {
                    onLoginSuccess(cleanEmail)
                } else {
                    Toast.makeText(
                        context,
                        "Email o contraseña incorrectos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(neonGreen)
            )
        ) {
            Text("Log in")
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No tenes cuenta? Crea una aquí",
            color = Color.White,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(neonGreen)
            )
        ) {
            Text("Sign up")
        }
    }
}