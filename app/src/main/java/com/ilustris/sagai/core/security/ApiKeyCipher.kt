package com.ilustris.sagai.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AES/GCM encryption for the user's Gemini API key, backed by the Android Keystore.
 *
 * The secret never leaves the Keystore — only the wrapped blob is written to disk, so a rooted
 * dump of `byok_datastore` yields ciphertext and nothing else.
 *
 * Every failure path returns null rather than throwing. A Keystore key can legitimately disappear
 * (screen-lock reset, restore onto another device, user clearing credentials), and the correct
 * reaction to an undecryptable blob is always "there is no key" — never a crash on a cold start.
 */
@Singleton
class ApiKeyCipher
    @Inject
    constructor() {
        /** @return `Base64(iv || ciphertext)`, or null if the Keystore refused to cooperate. */
        fun encrypt(plain: String): String? =
            try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey())
                val iv = cipher.iv
                val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
                Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
            } catch (e: Exception) {
                // Deliberately logs the failure class only — never the plaintext being wrapped.
                Timber.tag(TAG).e("Failed to encrypt API key: ${e.javaClass.simpleName}")
                null
            }

        /** @return the plaintext key, or null when the blob is absent, corrupt, or foreign. */
        fun decrypt(stored: String): String? =
            try {
                val raw = Base64.decode(stored, Base64.NO_WRAP)
                if (raw.size <= IV_LENGTH) {
                    null
                } else {
                    val iv = raw.copyOfRange(0, IV_LENGTH)
                    val ciphertext = raw.copyOfRange(IV_LENGTH, raw.size)
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(
                        Cipher.DECRYPT_MODE,
                        secretKey(),
                        GCMParameterSpec(TAG_LENGTH_BITS, iv),
                    )
                    String(cipher.doFinal(ciphertext), Charsets.UTF_8)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).w("Stored API key could not be decrypted: ${e.javaClass.simpleName}")
                null
            }

        /** Drops the Keystore entry so a removed key leaves nothing behind that could be unwrapped. */
        fun clear() {
            try {
                keyStore().deleteEntry(ALIAS)
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to delete Keystore entry: ${e.javaClass.simpleName}")
            }
        }

        private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

        private fun secretKey(): SecretKey {
            val existing = keyStore().getKey(ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val generator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
            generator.init(
                KeyGenParameterSpec
                    .Builder(
                        ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    // No setUserAuthenticationRequired: generation has to work on a cold start,
                    // before any screen the user could authenticate on has been drawn.
                    .build(),
            )
            return generator.generateKey()
        }

        companion object {
            private const val TAG = "🔐 ApiKeyCipher"
            private const val KEYSTORE = "AndroidKeyStore"
            private const val ALIAS = "sagai_byok_key"
            private const val TRANSFORMATION = "AES/GCM/NoPadding"
            private const val IV_LENGTH = 12
            private const val TAG_LENGTH_BITS = 128
        }
    }
