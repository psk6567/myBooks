package com.psk.mybooks.core.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class ConnectivityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityRepository {

    override fun isOnline(): Boolean {
        val manager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val capabilities: NetworkCapabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
