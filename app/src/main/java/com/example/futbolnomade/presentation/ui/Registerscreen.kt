package com.example.futbolnomade.presentation.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorFondo    = Color(0xFF171516)
private val ColorCampo    = Color(0xFF7A7A7A)
private val ColorVerde    = Color(0xFF00FF7F)
private val ColorError    = Color.Red
private val ColorTexto    = Color.White
private val ColorSubtexto = Color.White.copy(alpha = 0.75f)

@Composable
fun RegisterScreen(
    onRegistroExitoso: (nombreUsuario: String) -> Unit,
    onVolver: () -> Unit
) {
    var nombre    by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    var nombreError    by remember { mutableStateOf<String?>(null) }
    var emailError     by remember { mutableStateOf<String?>(null) }
    var passwordError  by remember { mutableStateOf<String?>(null) }
    var confirmarError by remember { mutableStateOf<String?>(null) }

    var verPassword  by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }

    fun validar(): Boolean {
        nombreError    = null
        emailError     = null
        passwordError  = null
        confirmarError = null

        if (nombre.trim().isEmpty())       { nombreError    = "Ingresá tu nombre";                          return false }
        if (nombre.trim().length < 2)      { nombreError    = "El nombre debe tener al menos 2 caracteres"; return false }
        if (email.trim().isEmpty())        { emailError     = "Ingresá tu email";                           return false }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { emailError = "Email inválido";       return false }
        if (password.trim().isEmpty())     { passwordError  = "Ingresá una contraseña";                     return false }
        if (password.trim().length < 6)    { passwordError  = "Mínimo 6 caracteres";                        return false }
        if (confirmar.trim().isEmpty())    { confirmarError = "Confirmá la contraseña";                     return false }
        if (confirmar.trim() != password.trim()) { confirmarError = "Las contraseñas no coinciden";        return false }

        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onVolver) {
                Icon(
                    // AutoMirrored evita el warning de deprecación
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTexto
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text("Crear cuenta", color = ColorTexto, fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(40.dp))

        // ── NOMBRE ───────────────────────────────────────────────────────
        CampoLabel("NOMBRE")
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; nombreError = null },
            placeholder = { Text("Juan Pérez", color = ColorSubtexto) },
            isError = nombreError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = campoColors()
        )
        ErrorTexto(nombreError)

        Spacer(Modifier.height(14.dp))

        // ── EMAIL ────────────────────────────────────────────────────────
        CampoLabel("EMAIL")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            placeholder = { Text("hello@reallygreatsite.com", color = ColorSubtexto) },
            isError = emailError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = campoColors()
        )
        ErrorTexto(emailError)

        Spacer(Modifier.height(14.dp))

        // ── CONTRASEÑA ───────────────────────────────────────────────────
        CampoLabel("CONTRASEÑA")
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            placeholder = { Text("••••••", color = ColorSubtexto) },
            visualTransformation = if (verPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        imageVector = if (verPassword) Icons.Filled.VisibilityOff
                        else             Icons.Filled.Visibility,
                        contentDescription = if (verPassword) "Ocultar" else "Mostrar",
                        tint = ColorVerde
                    )
                }
            },
            isError = passwordError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = campoColors()
        )
        ErrorTexto(passwordError)

        Spacer(Modifier.height(14.dp))

        // ── CONFIRMAR CONTRASEÑA ─────────────────────────────────────────
        CampoLabel("CONFIRMAR CONTRASEÑA")
        OutlinedTextField(
            value = confirmar,
            onValueChange = { confirmar = it; confirmarError = null },
            placeholder = { Text("••••••", color = ColorSubtexto) },
            visualTransformation = if (verConfirmar) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { verConfirmar = !verConfirmar }) {
                    Icon(
                        imageVector = if (verConfirmar) Icons.Filled.VisibilityOff
                        else              Icons.Filled.Visibility,
                        contentDescription = if (verConfirmar) "Ocultar" else "Mostrar",
                        tint = ColorVerde
                    )
                }
            },
            isError = confirmarError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = campoColors()
        )
        ErrorTexto(confirmarError)

        Spacer(Modifier.height(36.dp))

        OutlinedButton(
            onClick = { if (validar()) onRegistroExitoso(nombre.trim()) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTexto),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = SolidColor(ColorVerde)
            )
        ) {
            Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(20.dp))

        TextButton(onClick = onVolver) {
            Text("¿Ya tenés cuenta? Iniciá sesión", color = ColorTexto, fontSize = 11.sp)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CampoLabel(texto: String) {
    Text(
        text = texto,
        color = ColorVerde,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    )
}

@Composable
private fun ErrorTexto(error: String?) {
    error?.let {
        Text(text = it, color = ColorError, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun campoColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = ColorCampo,
    unfocusedContainerColor = ColorCampo,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    errorBorderColor        = ColorError,
    focusedTextColor        = ColorTexto,
    unfocusedTextColor      = ColorTexto,
    cursorColor             = ColorVerde
)