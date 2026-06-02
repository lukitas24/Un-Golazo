package com.example.futbolnomade.presentation.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val FondoOscuro  = Color(0xFF202020)
private val ColorCampo   = Color(0xFF2E2E2E)
private val Verde        = Color(0xFF82A820)
private val ColorTexto   = Color(0xFFEEEEEE)
private val ColorSub     = Color(0xFF999999)

@Composable
fun EditarPerfilScreen(
    nombreActual: String,
    emailActual: String,
    onGuardar: (nombre: String, email: String, password: String, uri: Uri?) -> Unit,
    onVolver: () -> Unit
) {
    var nombre   by remember { mutableStateOf(nombreActual) }
    var email    by remember { mutableStateOf(emailActual) }
    var password by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var verPass  by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var emailError  by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    fun validar(): Boolean {
        nombreError = null
        emailError  = null
        if (nombre.trim().isEmpty()) { nombreError = "El nombre no puede estar vacío"; return false }
        if (email.trim().isEmpty())  { emailError  = "El email no puede estar vacío";  return false }
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ColorTexto)
            }
            Text(
                text       = "Editar perfil",
                color      = ColorTexto,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Avatar ────────────────────────────────────────────────────────
        if (imageUri != null) {
            AsyncImage(
                model              = imageUri,
                contentDescription = "Foto de perfil",
                modifier           = Modifier.size(100.dp).clip(CircleShape)
            )
        } else {
            Box(
                modifier         = Modifier.size(100.dp).clip(CircleShape).background(Verde),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = nombre.firstOrNull()?.uppercase() ?: "?",
                    color      = Color.White,
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { imagePicker.launch("image/*") },
            shape   = RoundedCornerShape(10.dp),
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = Verde)
        ) {
            Text("Cambiar foto")
        }

        Spacer(Modifier.height(24.dp))

        // ── Nombre ────────────────────────────────────────────────────────
        CampoLabel("NOMBRE")
        OutlinedTextField(
            value         = nombre,
            onValueChange = { nombre = it; nombreError = null },
            isError       = nombreError != null,
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = campoColors()
        )
        nombreError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(14.dp))

        // ── Email ─────────────────────────────────────────────────────────
        CampoLabel("EMAIL")
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it; emailError = null },
            isError       = emailError != null,
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = campoColors()
        )
        emailError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(14.dp))

        // ── Nueva contraseña (opcional) ───────────────────────────────────
        CampoLabel("NUEVA CONTRASEÑA (opcional)")
        OutlinedTextField(
            value               = password,
            onValueChange       = { password = it },
            placeholder         = { Text("Dejar vacío para no cambiar", color = ColorSub, fontSize = 12.sp) },
            visualTransformation = if (verPass) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { verPass = !verPass }) {
                    Icon(
                        imageVector        = if (verPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        tint               = Verde
                    )
                }
            },
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
            shape      = RoundedCornerShape(12.dp),
            colors     = campoColors()
        )

        Spacer(Modifier.height(32.dp))

        // ── Guardar ───────────────────────────────────────────────────────
        Button(
            onClick = { if (validar()) onGuardar(nombre.trim(), email.trim(), password.trim(), imageUri) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Guardar cambios", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick  = onVolver,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = ColorTexto)
        ) {
            Text("Cancelar")
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CampoLabel(texto: String) {
    Text(
        text     = texto,
        color    = Verde,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    )
}

@Composable
private fun campoColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = ColorCampo,
    unfocusedContainerColor = ColorCampo,
    focusedBorderColor      = Verde,
    unfocusedBorderColor    = Color.Transparent,
    errorBorderColor        = Color.Red,
    focusedTextColor        = ColorTexto,
    unfocusedTextColor      = ColorTexto,
    cursorColor             = Verde
)