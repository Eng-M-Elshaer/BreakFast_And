package com.breakfast

import android.app.Application
import com.breakfast.managers.PreferenceManager
import com.breakfast.network.ApiClient

/**
 * Custom application class to initialize global dependencies such as PreferenceManager and
 * configure the ApiClient token provider. This ensures that the auth token is automatically
 * injected into network requests when available.
 */
class BreakfastApplication : Application() {

    lateinit var preferenceManager: PreferenceManager
        private set

    override fun onCreate() {
        super.onCreate()
        // Assign the singleton instance for easy access from composables or other classes
        instance = this
        preferenceManager = PreferenceManager(applicationContext)
        // Provide the token to the ApiClient when making network calls
        ApiClient.setTokenProvider { preferenceManager.getToken() }
        // Provide the language to the ApiClient. Default to English if not set.
        ApiClient.setLanguageProvider { preferenceManager.getLanguage() ?: "en" }
    }

    companion object {
        private lateinit var instance: BreakfastApplication
        fun get(): BreakfastApplication = instance
    }
}