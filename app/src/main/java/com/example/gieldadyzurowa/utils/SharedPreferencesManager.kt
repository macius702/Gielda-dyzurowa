package com.example.gieldadyzurowa.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString("JWT_TOKEN", token)
        editor.apply()
        Log.d("SharedPreferences", "JWT Token saved successfully")
    }

    fun getToken(): String? {
        return prefs.getString("JWT_TOKEN", null)?.also {
            Log.d("SharedPreferences", "JWT Token retrieved successfully")
        }
    }
}