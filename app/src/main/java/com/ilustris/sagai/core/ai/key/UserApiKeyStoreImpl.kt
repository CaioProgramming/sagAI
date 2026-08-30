package com.ilustris.sagai.core.ai.key

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ilustris.sagai.core.security.ApiKeyCipher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A DataStore of its own, separate from `settings_datastore`.
 *
 * Not tidiness — `allowBackup="true"` means preference files ride along to a new device while the
 * Android Keystore secret does not, so a restored blob is undecryptable. Isolating the key lets
 * `backup_rules.xml` exclude exactly this file without dropping every other preference the user
 * would want restored.
 */
private const val BYOK_DATASTORE_NAME = "byok_datastore"

val Context.byokDataStore by preferencesDataStore(BYOK_DATASTORE_NAME)

@Singleton
class UserApiKeyStoreImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val cipher: ApiKeyCipher,
        private val quotaStatusService: QuotaStatusService,
    ) : UserApiKeyStore {
        override fun observeState(): Flow<ApiKeyState> =
            context.byokDataStore.data.map { preferences ->
                val stored = preferences[ENCRYPTED_KEY]
                when {
                    // Also covers the restored-blob case: an undecryptable key is no key at all,
                    // and the user gets the setup screen rather than a crash loop.
                    stored.isNullOrBlank() || cipher.decrypt(stored) == null -> ApiKeyState.Missing

                    else ->
                        ApiKeyFailure
                            .findValue(preferences[INVALID_REASON])
                            ?.let { ApiKeyState.Invalidated(it) }
                            ?: ApiKeyState.Present
                }
            }

        override suspend fun getKeyNow(): String? =
            context.byokDataStore.data
                .first()[ENCRYPTED_KEY]
                ?.let { cipher.decrypt(it) }

        override suspend fun save(key: String) {
            val encrypted = cipher.encrypt(key.trim()) ?: error("Could not secure the API key")
            context.byokDataStore.edit { preferences ->
                preferences[ENCRYPTED_KEY] = encrypted
                preferences.remove(INVALID_REASON)
            }
            quotaStatusService.clearAll()
        }

        override suspend fun markInvalid(reason: ApiKeyFailure) {
            // A spent daily quota is not a bad key. Persisting it here would strand the user on
            // "your key was rejected" until they replaced a key that was working the whole time —
            // that case belongs to QuotaStatusService, which expires on its own.
            if (!reason.requiresNewKey) return
            context.byokDataStore.edit { it[INVALID_REASON] = reason.name }
        }

        override suspend fun clear() {
            context.byokDataStore.edit { preferences ->
                preferences.remove(ENCRYPTED_KEY)
                preferences.remove(INVALID_REASON)
            }
            quotaStatusService.clearAll()
            cipher.clear()
        }

        companion object {
            private val ENCRYPTED_KEY = stringPreferencesKey("encrypted_api_key")
            private val INVALID_REASON = stringPreferencesKey("api_key_invalid_reason")
        }
    }
