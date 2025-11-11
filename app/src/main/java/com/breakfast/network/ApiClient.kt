package com.breakfast.network

import com.google.android.datatransport.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

object ApiClient {
    private const val BASE_URL = "https://breakfast.restart-technology.com/api/v1/"

    // Token interceptor to add authorization headers
    // A provider function that supplies the current auth token. This will be set from your
    // PreferenceManager at runtime. If null, no Authorization header will be added.
    private var tokenProvider: (() -> String?)? = null

    // Language provider to supply the current "Accept-Language" header. When null,
    // the header will be omitted and the backend will rely on server defaults.
    private var languageProvider: (() -> String?)? = null

    // Optional network checker. Should return true if internet is available.
    private var networkChecker: (() -> Boolean)? = null

    fun setNetworkChecker(checker: () -> Boolean) {
        networkChecker = checker
    }

    /**
     * Set a callback that returns the latest auth token. Typically you will call this from
     * your Application or a setup class after creating the PreferenceManager.
     */
    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    /**
     * Set a callback that returns the user's preferred language. This value will be
     * sent in the "Accept-Language" header on all requests. If the provider returns
     * null or an empty string, the header is omitted.
     */
    fun setLanguageProvider(provider: () -> String?) {
        languageProvider = provider
    }

    private val authInterceptor = Interceptor { chain ->
        val hasNetwork = networkChecker?.invoke() ?: true
        if (!hasNetwork) {
            throw IOException("No internet connection")
        }
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
            .header("CLIENT-TYPE", "android")
            .header("CLIENT-VERSION", BuildConfig.VERSION_NAME)
        // Add the bearer token if available
        val token = tokenProvider?.invoke()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        // Add the Accept-Language header if provided
        val language = languageProvider?.invoke()
        if (!language.isNullOrEmpty()) {
            requestBuilder.header("Accept-Language", language)
        }
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}