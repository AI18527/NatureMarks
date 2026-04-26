package com.example.naturemarks.data.location

import android.Manifest
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

interface LocationRepositoryInterface {
    suspend fun getUserLocation(): Location?
    fun isWithinRadius(userLocation: Location, targetLocation: Location, radiusMeters: Float = 10f): Boolean
}

class LocationRepository(application: Application):
LocationRepositoryInterface {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getUserLocation(): Location? {
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        return fusedLocationClient.getCurrentLocation(locationRequest, null).await()
    }

    override fun isWithinRadius(
        userLocation: Location,
        targetLocation: Location,
        radiusMeters: Float
    ): Boolean {
        val distance = //userLocation.distanceTo(targetLocation)
            Location("").apply {
                latitude = 41.773611
                longitude = 23.406944
             }.distanceTo(targetLocation)
        return distance <= radiusMeters + userLocation.accuracy
    }
}