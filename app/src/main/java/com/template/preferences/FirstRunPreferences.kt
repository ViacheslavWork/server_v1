package com.template.preferences

import android.content.Context

object FirstRunPreferences {
    private const val PREF_FIRST_RUN = "first_run"
    private const val PREF_FIRST_RUN_FILE =
        "com.template.first_run"

    fun isFirstRun(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(PREF_FIRST_RUN_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(PREF_FIRST_RUN, true)
    }

    fun setIsFirstRun(isFirstRun: Boolean, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(PREF_FIRST_RUN_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(PREF_FIRST_RUN, isFirstRun).apply()
    }

}