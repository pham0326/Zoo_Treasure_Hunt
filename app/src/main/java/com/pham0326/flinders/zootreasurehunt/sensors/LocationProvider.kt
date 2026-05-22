package com.pham0326.flinders.zootreasurehunt.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationListener {

    companion object {
        private const val MIN_TIME_BETWEEN_UPDATES_MS = 5_000L
        private const val MIN_DISTANCE_BETWEEN_UPDATES_M = 5f
    }

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    private var isRegistered = false

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isRegistered) return
        if (!hasPermission()) return
        val lastKnown = tryGetLastKnownLocation()
        if (lastKnown != null) _currentLocation.value = lastKnown

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES_MS,
                MIN_DISTANCE_BETWEEN_UPDATES_M,
                this
            )
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES_MS,
                MIN_DISTANCE_BETWEEN_UPDATES_M,
                this
            )
        }
        isRegistered = true
    }

    fun stop() {
        if (!isRegistered) return
        locationManager.removeUpdates(this)
        isRegistered = false
    }

    fun distanceTo(targetLat: Double, targetLon: Double): Float? {
        val here = _currentLocation.value ?: return null
        val target = Location("zoo-target").apply {
            latitude = targetLat
            longitude = targetLon
        }
        return here.distanceTo(target)
    }

    @SuppressLint("MissingPermission")
    private fun tryGetLastKnownLocation(): Location? {
        return try {
            val gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            // Pick whichever was reported more recently.
            when {
                gps == null -> net
                net == null -> gps
                gps.time > net.time -> gps
                else -> net
            }
        } catch (e: SecurityException) {
            null
        }
    }

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location
    }
}