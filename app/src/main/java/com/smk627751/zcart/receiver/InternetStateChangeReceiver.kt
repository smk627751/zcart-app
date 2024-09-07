package com.smk627751.zcart.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smk627751.zcart.Utility

class InternetStateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.net.conn.CONNECTIVITY_CHANGE") {
            context?.let {
                if (!isConnectedToInternet(it) && !(context as Activity).isFinishing) {
                    MaterialAlertDialogBuilder(context)
                        .setMessage("No internet connection")
                        .setPositiveButton("OK") { _, _ ->
                            if(context is AppCompatActivity)
                            {
                                context.finish()
                            }
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }

    private fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
