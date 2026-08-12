package com.example.futbolnomade.presentation.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbolnomade.presentation.viewModel.AuthResult
import com.example.futbolnomade.presentation.viewModel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (
        nombre: String,
        email: String,
        uid: String,
        password: String
    ) -> Unit,
    onSignUpClick: () -> Unit = {},
    biometricAvailable: Boolean = false,
    biometricEmail: String? = null,
    biometricError: String? = null,
    onBiometricClick: () -> Unit = {}
) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var emailError by remember {
        mutableStateOf<String?>(null)
    }

    var passwordError by remember {
        mutableStateOf<String?>(null)
    }

    var loginError by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val darkBackground =
        Color(0xFF171516)

    val fieldGray =
        Color(0xFF7A7A7A)

    val neonGreen =
        Color(0xFF00FF7F)

    fun validarCampos(): Boolean {

        emailError = null
        passwordError = null
        loginError = null

        if (email.trim().isEmpty()) {

            emailError =
                "Ingresá tu email"

            return false
        }

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()
        ) {

            emailError =
                "Email inválido"

            return false
        }

        /*
         * IMPORTANTE:
         * password NO lleva trim().
         */
        if (password.isEmpty()) {

            passwordError =
                "Ingresá tu contraseña"

            return false
        }

        if (password.length < 6) {

            passwordError =
                "La contraseña debe tener al menos 6 caracteres"

            return false
        }

        return true
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(horizontal = 36.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            Modifier.height(72.dp)
        )

        Text(
            text = "Ingresar",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(56.dp)
        )

        // ============================================================
        // EMAIL
        // ============================================================

        Text(
            text = "EMAIL",
            color = neonGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.align(
                    Alignment.Start
                )
        )

        OutlinedTextField(
            value = email,

            onValueChange = {

                email = it
                emailError = null
                loginError = null
            },

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Email,
                    imeAction =
                        ImeAction.Next
                ),

            placeholder = {

                Text(
                    "hello@reallygreatsite.com",
                    color =
                        Color.White.copy(
                            alpha = 0.75f
                        )
                )
            },

            isError =
                emailError != null ||
                        loginError != null,

            singleLine = true,

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(14.dp),

            colors =
                OutlinedTextFieldDefaults.colors(

                    focusedContainerColor =
                        fieldGray,

                    unfocusedContainerColor =
                        fieldGray,

                    focusedBorderColor =
                        Color.Transparent,

                    unfocusedBorderColor =
                        Color.Transparent,

                    errorBorderColor =
                        Color.Red,

                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    cursorColor =
                        neonGreen
                )
        )

        emailError?.let { error ->

            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier =
                    Modifier.align(
                        Alignment.Start
                    )
            )
        }

        Spacer(
            Modifier.height(14.dp)
        )

        // ============================================================
        // PASSWORD
        // ============================================================

        Text(
            text = "CONTRASEÑA",
            color = neonGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.align(
                    Alignment.Start
                )
        )

        OutlinedTextField(
            value = password,

            onValueChange = {

                /*
                 * Guardamos EXACTAMENTE lo escrito.
                 */
                password = it

                passwordError = null
                loginError = null
            },

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Password,
                    imeAction =
                        ImeAction.Done
                ),

            placeholder = {

                Text(
                    "••••••",
                    color =
                        Color.White.copy(
                            alpha = 0.75f
                        )
                )
            },

            visualTransformation =
                PasswordVisualTransformation(),

            isError =
                passwordError != null ||
                        loginError != null,

            singleLine = true,

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(14.dp),

            colors =
                OutlinedTextFieldDefaults.colors(

                    focusedContainerColor =
                        fieldGray,

                    unfocusedContainerColor =
                        fieldGray,

                    focusedBorderColor =
                        Color.Transparent,

                    unfocusedBorderColor =
                        Color.Transparent,

                    errorBorderColor =
                        Color.Red,

                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    cursorColor =
                        neonGreen
                )
        )

        passwordError?.let { error ->

            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier =
                    Modifier.align(
                        Alignment.Start
                    )
            )
        }

        loginError?.let { error ->

            Spacer(
                Modifier.height(4.dp)
            )

            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier =
                    Modifier.align(
                        Alignment.Start
                    )
            )
        }

        biometricError?.let { error ->

            Spacer(
                Modifier.height(6.dp)
            )

            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier =
                    Modifier.align(
                        Alignment.Start
                    )
            )
        }

        Spacer(
            Modifier.height(30.dp)
        )

        // ============================================================
        // LOGIN
        // ============================================================

        OutlinedButton(
            onClick = {

                if (!validarCampos()) {
                    return@OutlinedButton
                }

                isLoading = true

                authViewModel.login(

                    /*
                     * Email sí puede limpiarse.
                     */
                    email = email.trim(),

                    /*
                     * PASSWORD NO.
                     */
                    password = password

                ) { result ->

                    isLoading = false

                    when (result) {

                        is AuthResult.Success -> {

                            val usuario =
                                authViewModel
                                    .usuarioActual

                            if (usuario == null) {

                                loginError =
                                    "No se pudo cargar el usuario."

                            } else {

                                onLoginSuccess(
                                    usuario.nombre
                                        .ifBlank {
                                            usuario.email
                                        },
                                    usuario.email,
                                    usuario.uid,

                                    /*
                                     * Guardamos exactamente
                                     * la contraseña autenticada.
                                     */
                                    password
                                )
                            }
                        }

                        is AuthResult.Error -> {

                            loginError =
                                result.mensaje
                        }
                    }
                }
            },

            enabled =
                !isLoading,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),

            shape =
                RoundedCornerShape(14.dp),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        contentColor =
                            Color.White
                    ),

            border =
                ButtonDefaults
                    .outlinedButtonBorder
                    .copy(
                        width = 2.dp,
                        brush =
                            SolidColor(
                                neonGreen
                            )
                    )
        ) {

            if (isLoading) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = neonGreen
                )

            } else {

                Text("Ingresar")
            }
        }

        // ============================================================
        // BIOMETRÍA
        // ============================================================

        if (biometricAvailable) {

            Spacer(
                Modifier.height(20.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                HorizontalDivider(
                    modifier =
                        Modifier.weight(1f),
                    color =
                        Color.White.copy(
                            alpha = 0.25f
                        )
                )

                Text(
                    text = "  O  ",
                    color =
                        Color.White.copy(
                            alpha = 0.65f
                        ),
                    fontSize = 12.sp
                )

                HorizontalDivider(
                    modifier =
                        Modifier.weight(1f),
                    color =
                        Color.White.copy(
                            alpha = 0.25f
                        )
                )
            }

            Spacer(
                Modifier.height(20.dp)
            )

            OutlinedButton(
                onClick =
                    onBiometricClick,

                enabled =
                    !isLoading,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults
                        .outlinedButtonColors(
                            contentColor =
                                neonGreen
                        ),

                border =
                    ButtonDefaults
                        .outlinedButtonBorder
                        .copy(
                            width = 2.dp,
                            brush =
                                SolidColor(
                                    neonGreen
                                )
                        )
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier =
                        Modifier.size(25.dp)
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Text(
                    text =
                        "Ingresar con biometría",
                    color =
                        Color.White
                )
            }

            biometricEmail
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { savedEmail ->

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Cuenta vinculada: $savedEmail",
                        color =
                            Color.White.copy(
                                alpha = 0.65f
                            ),
                        fontSize = 11.sp
                    )
                }
        }

        Spacer(
            Modifier.height(18.dp)
        )

        Text(
            text =
                "¿No tenés cuenta? Creá una aquí",
            color =
                Color.White,
            fontSize =
                11.sp
        )

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick =
                onSignUpClick,

            enabled =
                !isLoading,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),

            shape =
                RoundedCornerShape(14.dp),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        contentColor =
                            Color.White
                    ),

            border =
                ButtonDefaults
                    .outlinedButtonBorder
                    .copy(
                        width = 2.dp,
                        brush =
                            SolidColor(
                                neonGreen
                            )
                    )
        ) {

            Text("Registrarse")
        }
    }
}