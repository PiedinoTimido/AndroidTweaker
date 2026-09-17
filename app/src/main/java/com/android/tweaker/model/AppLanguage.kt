package com.android.tweaker.model

import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    ITALIAN("it", "Italian", "Italiano"),
    GERMAN("de", "German", "Deutsch"),
    FRENCH("fr", "French", "Français"),
    SPANISH("es", "Spanish", "Español"),
    PORTUGUESE("pt", "Portuguese", "Português"),
    RUSSIAN("ru", "Russian", "Русский"),
    HINDI("hi", "Hindi", "हिन्दी");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }

        fun getSystemDefault(): AppLanguage {
            val deviceLang = Locale.getDefault().language.lowercase()
            return values().firstOrNull { it.code == deviceLang } ?: ENGLISH
        }
    }
}
