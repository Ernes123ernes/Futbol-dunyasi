package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String) {
    DARK("Karanlık Mod"),
    LIGHT("Aydınlık Mod"),
    SYSTEM("Sistem Teması")
}

class ThemeManager private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("football_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            val saved = prefs.getString("key_theme_mode", ThemeMode.DARK.name)
            ThemeMode.valueOf(saved ?: ThemeMode.DARK.name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("key_theme_mode", mode.name).apply()
    }

    fun toggleDarkLight() {
        val current = _themeMode.value
        val next = if (current == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(next)
    }

    companion object {
        @Volatile
        private var INSTANCE: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
