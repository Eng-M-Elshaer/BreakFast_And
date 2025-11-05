package com.breakfast.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.breakfast.models.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.core.content.edit

/**
 * A helper class that wraps encrypted SharedPreferences to store sensitive data such as
 * authentication tokens and user profiles. This manager uses Jetpack Security's
 * EncryptedSharedPreferences to ensure values are encrypted on disk.
 *
 * The stored user object is serialized/deserialized using Moshi.
 */
class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences
    private val userAdapter: com.squareup.moshi.JsonAdapter<User>

    init {
        // Create or retrieve a master key for encryption/decryption
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            "breakfast_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        userAdapter = moshi.adapter(User::class.java)
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_ASKED_NOTIFICATION_PERMISSION = "asked_notification_permission"
    }

    /**
     * Persist the authentication token. Passing null removes the token.
     */
    fun saveToken(token: String?) {
        sharedPreferences.edit {
            if (token == null) remove(KEY_TOKEN) else putString(KEY_TOKEN, token)
        }
    }

    /**
     * Persist the FCM token used for push notifications.
     */
    fun saveFcmToken(token: String?) {
        sharedPreferences.edit {
            if (token == null) remove(KEY_FCM_TOKEN) else putString(KEY_FCM_TOKEN, token)
        }
    }

    /**
     * Retrieve the persisted FCM token, or null if not set.
     */
    fun getFcmToken(): String? = sharedPreferences.getString(KEY_FCM_TOKEN, null)

    /**
     * Retrieve the persisted authentication token, or null if not set.
     */
    fun getToken(): String? = sharedPreferences.getString(KEY_TOKEN, null)

    /**
     * Persist the user object by serializing it to JSON. Passing null removes the user.
     */
    fun saveUser(user: User?) {
        val json = user?.let { userAdapter.toJson(it) }
        sharedPreferences.edit {
            if (json == null) remove(KEY_USER) else putString(KEY_USER, json)
        }
    }

    /**
     * Retrieve the persisted user object, or null if not set or deserialization fails.
     */
    fun getUser(): User? {
        val json = sharedPreferences.getString(KEY_USER, null) ?: return null
        return try {
            userAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear all stored values including token and user.
     */
    fun clear() {
        sharedPreferences.edit { clear() }
    }

    /**
     * Persist the user's preferred language. Passing null removes the language and defaults
     * to English. The language code should follow ISO 639-1 (e.g., "en" or "ar").
     */
    fun saveLanguage(language: String?) {
        sharedPreferences.edit {
            if (language == null) remove(KEY_LANGUAGE) else putString(KEY_LANGUAGE, language)
        }
    }

    /**
     * Retrieve the stored language code or null if not set. Consumers should
     * fallback to a sensible default such as "en" when null.
     */
    fun getLanguage(): String? = sharedPreferences.getString(KEY_LANGUAGE, null)

    /**
     * Check if the app has previously requested notification permission from the user.
     * This helps prevent repeatedly asking for permission if the user has already denied it.
     *
     * @return true if permission has been requested before, false otherwise
     */
    fun hasAskedForNotificationPermission(): Boolean {
        return sharedPreferences.getBoolean(KEY_ASKED_NOTIFICATION_PERMISSION, false)
    }

    /**
     * Mark that the app has requested notification permission from the user.
     * This should be called before launching the permission request dialog.
     *
     * @param asked true to mark as asked, false to reset the flag
     */
    fun setAskedForNotificationPermission(asked: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_ASKED_NOTIFICATION_PERMISSION, asked)
        }
    }
}