package com.breakfast.utils

import android.util.Patterns
import android.webkit.URLUtil

object Validator {

    // region — Regexes (compiled once)
    private val NAME_REGEX = Regex("^[a-zA-Z\\s]+$")
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,}$")
    // Same as iOS pattern: local@domain.tld (2–64 TLD)
    private val EMAIL_REGEX = Regex("^[A-Z0-9a-z._%+-]+@[A-Z0-9a-z.-]+\\.[A-Za-z]{2,64}$")
    private val PHONE_10_REGEX = Regex("^[0-9]{10}$")
    // Egyptian mobile numbers: 010 / 011 / 012 / 015 + 8 digits = 11 total
    private val EGYPT_PHONE_REGEX = Regex("^(010|011|012|015)[0-9]{8}$")
    // At least 8 chars, 1 lower, 1 upper, 1 digit
    private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    // Instapay links:
    // - instapay.me / instapay.eg with a path token
    // - ipn.eg/.../instapay/<token>
    private val INSTAPAY_REGEX = Regex(
        pattern = "^https?://(www\\.)?((instapay\\.me|instapay\\.eg)/[A-Za-z0-9._-]+|ipn\\.eg/.*/instapay/.+)$",
        option = RegexOption.IGNORE_CASE
    )
    // endregion

    // MARK: - Validation Methods

    fun isValidFullName(name: String): Boolean =
        NAME_REGEX.matches(name.trim())

    fun isValidUsername(username: String): Boolean =
        USERNAME_REGEX.matches(username.trim())

    fun isValidEmail(email: String): Boolean {
        val e = email.trim()
        // Keep parity with iOS regex AND leverage Android's built-in pattern for robustness
        return EMAIL_REGEX.matches(e) && Patterns.EMAIL_ADDRESS.matcher(e).matches()
    }

    /**
     * InstaPay link validation.
     * Matches:
     *  - https://instapay.me/<token>
     *  - https://instapay.eg/<token>
     *  - https://ipn.eg/<any>/instapay/<token>
     *  Case-insensitive, trims whitespace, and also checks Android URL utilities.
     */
    fun isValidInstaPayLink(link: String): Boolean {
        val u = link.trim()
        if (u.isEmpty()) return false
        // Must match our explicit patterns AND be a valid network URL in Android sense
        return INSTAPAY_REGEX.matches(u) &&
                URLUtil.isNetworkUrl(u) &&
                Patterns.WEB_URL.matcher(u).matches()
    }

    /** Basic 10-digit numeric phone (to mirror iOS example). */
    fun isValidPhoneNumber(phoneNumber: String): Boolean =
        PHONE_10_REGEX.matches(phoneNumber.trim())

    /** Egyptian mobile format: 010/011/012/015 + 8 digits. */
    fun isValidEgyptianPhoneNumber(phoneNumber: String): Boolean =
        EGYPT_PHONE_REGEX.matches(phoneNumber.trim())

    fun isValidPassword(password: String): Boolean =
        PASSWORD_REGEX.matches(password)

    /**
     * URL validation: check scheme + Android URL utilities.
     * (Equivalent spirit to iOS canOpenURL without requiring a Context.)
     */
    fun isValidURL(urlString: String): Boolean {
        val u = urlString.trim()
        if (u.isEmpty()) return false
        val hasScheme = u.startsWith("http://", true) ||
                u.startsWith("https://", true) ||
                u.startsWith("mailto:", true) ||
                u.startsWith("tel:", true)
        return hasScheme && URLUtil.isValidUrl(u) && Patterns.WEB_URL.matcher(u).matches()
    }

    fun isEmpty(text: String): Boolean =
        text.trim().isEmpty()
}