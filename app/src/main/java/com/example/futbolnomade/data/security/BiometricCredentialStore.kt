package com.example.futbolnomade.data.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class BiometricCredentialStore(
    context: Context
) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val keyStore: KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    fun hasLinkedAccount(): Boolean {
        return preferences.contains(KEY_UID) &&
                preferences.contains(KEY_EMAIL) &&
                preferences.contains(KEY_ENCRYPTED_PASSWORD) &&
                preferences.contains(KEY_INITIALIZATION_VECTOR) &&
                keyStore.containsAlias(KEY_ALIAS)
    }

    fun getLinkedAccount(): BiometricAccountInfo? {
        if (!hasLinkedAccount()) {
            return null
        }

        val uid = preferences.getString(KEY_UID, null)
            ?: return null

        val email = preferences.getString(KEY_EMAIL, null)
            ?: return null

        return BiometricAccountInfo(
            uid = uid,
            email = email
        )
    }

    /**
     * Genera una clave nueva para la única cuenta biométrica del dispositivo.
     * El Cipher devuelto debe enviarse a BiometricPrompt mediante CryptoObject.
     */
    fun createEncryptionCipher(): Cipher {
        deleteSecretKey()

        val secretKey = generateSecretKey()

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.ENCRYPT_MODE,
                secretKey
            )
        }
    }

    /**
     * Prepara el Cipher para descifrar la contraseña ya vinculada.
     * El Cipher todavía no puede ejecutar doFinal hasta que Android
     * autorice la operación mediante BiometricPrompt.
     */
    fun createDecryptionCipher(): Cipher {
        val encodedIv = preferences.getString(
            KEY_INITIALIZATION_VECTOR,
            null
        ) ?: throw IllegalStateException(
            "No existe un vector de inicialización guardado."
        )

        val secretKey = getExistingSecretKey()
            ?: throw IllegalStateException(
                "La clave biométrica ya no está disponible."
            )

        val initializationVector = Base64.decode(
            encodedIv,
            Base64.NO_WRAP
        )

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                secretKey,
                GCMParameterSpec(
                    GCM_TAG_LENGTH,
                    initializationVector
                )
            )
        }
    }

    /**
     * Debe llamarse únicamente con el Cipher devuelto por un
     * BiometricPrompt exitoso.
     */
    fun saveCredentials(
        uid: String,
        email: String,
        password: String,
        authenticatedCipher: Cipher
    ) {
        require(uid.isNotBlank()) {
            "El UID no puede estar vacío."
        }
        require(email.isNotBlank()) {
            "El email no puede estar vacío."
        }
        require(password.isNotEmpty()) {
            "La contraseña no puede estar vacía."
        }

        val encryptedPassword = authenticatedCipher.doFinal(
            password.toByteArray(Charsets.UTF_8)
        )

        preferences.edit()
            .putString(KEY_UID, uid)
            .putString(KEY_EMAIL, email.trim().lowercase())
            .putString(
                KEY_ENCRYPTED_PASSWORD,
                Base64.encodeToString(
                    encryptedPassword,
                    Base64.NO_WRAP
                )
            )
            .putString(
                KEY_INITIALIZATION_VECTOR,
                Base64.encodeToString(
                    authenticatedCipher.iv,
                    Base64.NO_WRAP
                )
            )
            .commit()
    }

    /**
     * Debe llamarse únicamente con el Cipher devuelto por un
     * BiometricPrompt exitoso.
     */
    fun decryptCredentials(
        authenticatedCipher: Cipher
    ): BiometricCredentials {
        val uid = preferences.getString(KEY_UID, null)
            ?: throw IllegalStateException(
                "No existe un UID biométrico guardado."
            )

        val email = preferences.getString(KEY_EMAIL, null)
            ?: throw IllegalStateException(
                "No existe un email biométrico guardado."
            )

        val encodedPassword = preferences.getString(
            KEY_ENCRYPTED_PASSWORD,
            null
        ) ?: throw IllegalStateException(
            "No existe una contraseña cifrada."
        )

        val encryptedPassword = Base64.decode(
            encodedPassword,
            Base64.NO_WRAP
        )

        val decryptedPassword = String(
            authenticatedCipher.doFinal(encryptedPassword),
            Charsets.UTF_8
        )

        return BiometricCredentials(
            uid = uid,
            email = email,
            password = decryptedPassword
        )
    }

    fun clearLinkedAccount() {
        preferences.edit()
            .clear()
            .commit()

        deleteSecretKey()
    }

    private fun getExistingSecretKey(): SecretKey? {
        val entry = keyStore.getEntry(
            KEY_ALIAS,
            null
        ) as? KeyStore.SecretKeyEntry

        return entry?.secretKey
    }

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(
                KeyProperties.BLOCK_MODE_GCM
            )
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(
                -1
            )
        }

        keyGenerator.init(builder.build())

        return keyGenerator.generateKey()
    }

    private fun deleteSecretKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    private companion object {
        const val PREFERENCES_NAME =
            "futbol_nomade_biometric_credentials"

        const val KEY_ALIAS =
            "futbol_nomade_biometric_login_key"

        const val KEY_UID =
            "biometric_user_uid"

        const val KEY_EMAIL =
            "biometric_user_email"

        const val KEY_ENCRYPTED_PASSWORD =
            "biometric_encrypted_password"

        const val KEY_INITIALIZATION_VECTOR =
            "biometric_initialization_vector"

        const val ANDROID_KEYSTORE =
            "AndroidKeyStore"

        const val TRANSFORMATION =
            "AES/GCM/NoPadding"

        const val GCM_TAG_LENGTH = 128
    }
}