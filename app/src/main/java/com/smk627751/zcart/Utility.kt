package com.smk627751.zcart

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.MediaType
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.smk627751.zcart.Repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.UUID

object Utility {
    fun generateOrderId(): String {
        return UUID.randomUUID().toString()
    }
    fun makeToast(context : Context, msg : String)
    {
        Toast.makeText(context,msg, Toast.LENGTH_SHORT).show()
    }
    fun formatTime(time : Long) : String
    {
        return SimpleDateFormat("dd/MM/yyyy").format(time)
    }
    fun hideSoftKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val window = (context as Activity).window
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    fun showAlertDialog(context: Context,title : String,positive : () -> Unit)
    {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setPositiveButton("OK") { _, _ ->
                positive()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}