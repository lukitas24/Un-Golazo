package com.example.futbolnomade.presentation.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

class BiometricAuthManager(
    private val activity: FragmentActivity
) {

    fun getAvailability(): BiometricAvailability {
        val result = BiometricManager
            .from(activity)
            .canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )

        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricAvailability.Available
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricAvailability.NotEnrolled
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                BiometricAvailability.NoHardware
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                BiometricAvailability.TemporarilyUnavailable
            }

            else -> {
                BiometricAvailability.Unsupported
            }
        }
    }

    fun authenticateForEncryption(
        cipher: Cipher,
        accountEmail: String,
        onSuccess: (Cipher) -> Unit,
        onCancelled: () -> Unit,
        onError: (String) -> Unit
    ) {
        authenticate(
            cipher = cipher,
            title = "Vincular biometría",
            subtitle = "Confirmá tu identidad para $accountEmail",
            negativeButton = "Cancelar",
            onSuccess = onSuccess,
            onCancelled = onCancelled,
            onError = onError
        )
    }

    fun authenticateForLogin(
        cipher: Cipher,
        accountEmail: String,
        onSuccess: (Cipher) -> Unit,
        onCancelled: () -> Unit,
        onError: (String) -> Unit
    ) {
        authenticate(
            cipher = cipher,
            title = "Ingresar a Fútbol Nómade",
            subtitle = "Continuar como $accountEmail",
            negativeButton = "Usar email y contraseña",
            onSuccess = onSuccess,
            onCancelled = onCancelled,
            onError = onError
        )
    }

    private fun authenticate(
        cipher: Cipher,
        title: String,
        subtitle: String,
        negativeButton: String,
        onSuccess: (Cipher) -> Unit,
        onCancelled: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor =
            ContextCompat.getMainExecutor(activity)

        val callback =
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    val authenticatedCipher =
                        result.cryptoObject?.cipher

                    if (authenticatedCipher == null) {
                        onError(
                            "Android no devolvió la operación criptográfica."
                        )
                        return
                    }

                    onSuccess(authenticatedCipher)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    /*
                     * Android mantiene abierto el diálogo y permite
                     * volver a intentar. No lo tratamos como error terminal.
                     */
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(
                        errorCode,
                        errString
                    )

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> {
                            onCancelled()
                        }

                        else -> {
                            onError(
                                errString.toString()
                            )
                        }
                    }
                }
            }

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            callback
        )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                .setNegativeButtonText(
                    negativeButton
                )
                .setConfirmationRequired(false)
                .build()

        biometricPrompt.authenticate(
            promptInfo,
            BiometricPrompt.CryptoObject(cipher)
        )
    }
}

sealed interface BiometricAvailability {

    data object Available :
        BiometricAvailability

    data object NotEnrolled :
        BiometricAvailability

    data object NoHardware :
        BiometricAvailability

    data object TemporarilyUnavailable :
        BiometricAvailability

    data object Unsupported :
        BiometricAvailability
}