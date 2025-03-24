package com.exa.android.reflekt.loopit.util.application


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

class NetworkCallbackReceiver(
    private val onNetworkChange : (Boolean) -> Unit
) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        onNetworkChange(true)
    }

    override fun onLost(network: Network) {
        onNetworkChange(false)
    }
}


/* // I'm operating it directly in Disposable Effect bec need to unregister on Dispose
fun registerNetworkCallback(context : Context, onNetworkChange: (Boolean) -> Unit){
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = NetworkCallbackReceiver(onNetworkChange)
    connectivityManager.registerDefaultNetworkCallback(callback)
}*/