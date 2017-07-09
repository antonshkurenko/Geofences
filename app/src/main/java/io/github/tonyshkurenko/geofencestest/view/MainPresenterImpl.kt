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

package io.github.tonyshkurenko.geofencestest.view

import android.Manifest
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.tbruyelle.rxpermissions2.RxPermissions
import io.github.tonyshkurenko.geofencestest.di.ActivityScope
import io.github.tonyshkurenko.geofencestest.model.LocationManager
import io.github.tonyshkurenko.geofencestest.model.WifiManager
import io.github.tonyshkurenko.geofencestest.util.location
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import javax.inject.Inject

/**
 * Project: GeofencesTest
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 * Follow me: @tonyshkurenko
 *
 * @author Anton Shkurenko
 * @since 7/9/17
 */
@ActivityScope class MainPresenterImpl @Inject constructor(
    val view: MainView,
    val rxPermissions: RxPermissions,
    val locationManager: LocationManager,
    val wifiManager: WifiManager
) : MainPresenter {

  val compositeDisposable = CompositeDisposable()

  override fun create() {
    val permissionDisposable = rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE)
        .subscribe {
          view.reactOnPermissions(it)

          if (!it) {
            view.close()
          } else {
            locationManager.connect()
          }
        }

    compositeDisposable.add(permissionDisposable)
  }

  override fun resume() {

    view.updateYourWifi(wifiManager.currentWifi?.ssid ?: "unknown")

    val locationsDisposable = locationManager.locations
        .observeOn(AndroidSchedulers.mainThread())
        .map { LatLng(it.latitude, it.longitude) }
        .subscribe { view.updateYourLocation(it) }

    compositeDisposable.add(locationsDisposable)

    val locationClicksDisposable = view.locationClicks
        .subscribe { view.selectNewLocation() }

    compositeDisposable.add(locationClicksDisposable)

    val locationStatusObs: Observable<Boolean> = Observable.combineLatest(
        locationManager.locations,
        locationManager.selectedLocations,
        view.radiusChange, Function3 { yourLocation, selectedLocation, radius ->

      val distanceTo = yourLocation.distanceTo(
          selectedLocation.location)

      view.updateDistanceBetween(distanceTo)
      distanceTo < radius
    })

    val insideGeofenceObs: Observable<Boolean> = Observable.combineLatest(
        locationStatusObs.startWith(false),
        view.wifiTextChange.map {
          wifiManager.currentWifi?.ssid?.toLowerCase()?.contains(it.toLowerCase()) == true
        }.startWith(false), BiFunction { location, wifi -> location || wifi })

    val insideGeofenceDisposable = insideGeofenceObs
        .subscribe { view.updateGeofenceStatus(it) }

    compositeDisposable.add(insideGeofenceDisposable)
  }

  override fun pause() = compositeDisposable.clear()

  override fun onNewLocationSelected(place: Place?) {

    place?.let {
      locationManager.selectPlace(it)
      view.updateSelectedLocation(it.latLng)
    }
  }
}