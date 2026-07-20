package com.example.futbolnomade.presentation.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.futbolnomade.data.security.BiometricAccountInfo
import com.example.futbolnomade.data.security.BiometricCredentialStore
import com.example.futbolnomade.data.security.BiometricCredentials
import javax.crypto.Cipher

class BiometricLoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val credentialStore =
        BiometricCredentialStore(application)

    var linkedAccount by mutableStateOf(
        credentialStore.getLinkedAccount()
    )
        private set

    val hasLinkedAccount: Boolean
        get() = linkedAccount != null

    fun isLinkedTo(uid: String?): Boolean {
        return uid != null &&
                linkedAccount?.uid == uid
    }

    fun createEncryptionCipher(): Cipher {
        return credentialStore.createEncryptionCipher()
    }

    fun createDecryptionCipher(): Cipher {
        return credentialStore.createDecryptionCipher()
    }

    fun saveLinkedCredentials(
        uid: String,
        email: String,
        password: String,
        authenticatedCipher: Cipher
    ) {
        credentialStore.saveCredentials(
            uid = uid,
            email = email,
            password = password,
            authenticatedCipher = authenticatedCipher
        )

        linkedAccount =
            credentialStore.getLinkedAccount()
    }

    fun decryptCredentials(
        authenticatedCipher: Cipher
    ): BiometricCredentials {
        return credentialStore.decryptCredentials(
            authenticatedCipher
        )
    }

    fun unlinkAccount() {
        credentialStore.clearLinkedAccount()
        linkedAccount = null
    }

    fun refresh() {
        linkedAccount =
            credentialStore.getLinkedAccount()
    }
}