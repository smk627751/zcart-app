package com.smk627751.zcart.sharedpreferences

import android.content.Context

class Session(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    fun saveSession(id : String)
    {
        editor.putString("currentUserId",id).commit()
    }
    fun getSession() : String?
    {
        return sharedPreferences.getString("currentUserId", "")
    }
}