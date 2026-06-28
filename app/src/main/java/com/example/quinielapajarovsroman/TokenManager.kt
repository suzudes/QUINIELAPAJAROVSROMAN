package com.example.quinielapajarovsroman

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("quiniela_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("user_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("user_token", null)

    fun clear() {
        prefs.edit().remove("user_token").apply()
    }
}
