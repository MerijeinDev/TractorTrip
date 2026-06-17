package farmyard.tractortrip.lab

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

object NetworkUtils {

    fun isOnline(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Observes network availability. Callback fires on a binder thread — marshal to main thread before touching UI. */
    fun observeOnline(context: Context, onOnline: () -> Unit): ConnectivityManager.NetworkCallback {
        val manager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onOnline()
            }
        }
        manager.registerNetworkCallback(request, callback)
        return callback
    }

    fun stopObserving(context: Context, callback: ConnectivityManager.NetworkCallback) {
        val manager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            manager.unregisterNetworkCallback(callback)
        } catch (_: IllegalArgumentException) {
            // callback was already unregistered
        }
    }
}
