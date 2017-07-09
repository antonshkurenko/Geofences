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

  override fun resume() {

    val locationsDisposable = locationManager.locations
        .observeOn(AndroidSchedulers.mainThread())
        .map { LatLng(it.latitude, it.longitude) }
        .doOnNext { view.updateYourLocation(it) }
        .subscribe()

    compositeDisposable.add(locationsDisposable)

    val locationClicksDisposable = view.locationClicks
        .compose(rxPermissions.ensure(Manifest.permission.ACCESS_COARSE_LOCATION))
        .subscribe {
          granted ->
          if (granted) {
            locationManager.connect()
            view.selectNewLocation()
          } else {
            view.reactOnPermissions(granted)
          }
        }

    compositeDisposable.add(locationClicksDisposable)

    val wifiClicksDisposable = view.wifiTextChange
        .compose(rxPermissions.ensure(Manifest.permission.ACCESS_WIFI_STATE))
        .subscribe {
          granted ->
          if (granted) {
            view.updateYourWifi(wifiManager.currentWifi?.ssid ?: "unknown")
          } else {
            view.reactOnPermissions(granted)
          }
        }

    compositeDisposable.add(wifiClicksDisposable)

    val locationStatusObs: Observable<Boolean> = Observable.combineLatest(
        locationManager.locations,
        locationManager.selectedLocations,
        view.radiusChange, Function3 { yourLocation, selectedLocation, radius ->
      yourLocation.distanceTo(
          selectedLocation.location) < radius
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
      locationManager.selectPlace(place)
      view.updateSelectedLocation(place.latLng)
    }
  }
}