/*
 * Copyright 2017 Anton Shkurenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.tonyshkurenko.geofencestest.model

import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Project: GeofencesTest
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 * Follow me: @tonyshkurenko
 *
 * @author Anton Shkurenko
 * @since 7/9/17
 */

@Singleton class LocationManagerImpl @Inject constructor(
    val googleApiClient: GoogleApiClient) : LocationManager,
    ConnectionCallbacks {

  private val locationSubject: BehaviorSubject<Location> = BehaviorSubject.create()
  override val locations: Observable<Location> = locationSubject.hide()

  private val selectedLocationSubject: BehaviorSubject<Place> = BehaviorSubject.create()
  override val selectedLocations: Observable<LatLng> = selectedLocationSubject
      .map { it.latLng }
      .hide()

  override fun connect() {
    googleApiClient.registerConnectionCallbacks(this)
    googleApiClient.registerConnectionFailedListener { Timber.w("onConnectionFailed: $it") }

    googleApiClient.connect()
  }

  override fun selectPlace(place: Place) = selectedLocationSubject.onNext(place)

  override fun onConnected(p0: Bundle?) {
    Timber.d("onConnected() called with: $p0")

    val locationRequest = LocationRequest()
        .setInterval(TimeUnit.MINUTES.toMillis(30))
        .setFastestInterval(TimeUnit.MINUTES.toMillis(1))
        .setSmallestDisplacement(10f)
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

    LocationServices
        .FusedLocationApi
        .requestLocationUpdates(googleApiClient,
            locationRequest) { locationSubject.onNext(it) }
  }

  override fun onConnectionSuspended(cause: Int) = Timber.d(
      "onConnectionSuspended() called with: $cause")

}