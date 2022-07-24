package com.template.preferences

import android.content.Context

object ServerAnswerPreferences {
    private const val PREF_SERVER_ANSWER = "SERVER_ANSWER"
    private const val PREF_SERVER_ANSWER_FILE =
        "com.template.SERVER_ANSWER"
    const val ERROR = "com.template.error"

    fun getServerAnswer(context: Context): String? {
        val sharedPreferences =
            context.getSharedPreferences(PREF_SERVER_ANSWER_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREF_SERVER_ANSWER, null)
    }

    fun setServerAnswer(serverAnswer: String, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(PREF_SERVER_ANSWER_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PREF_SERVER_ANSWER, serverAnswer).apply()
    }

}