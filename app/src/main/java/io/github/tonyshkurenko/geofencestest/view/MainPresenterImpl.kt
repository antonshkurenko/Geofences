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
import android.location.Location
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
import io.reactivex.functions.Function4
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
          }
          view.reactOnPermissions(granted)
        }

    compositeDisposable.add(locationClicksDisposable)

    val wifiClicksDisposable = view.wifiTextChange
        .compose(rxPermissions.ensure(Manifest.permission.ACCESS_WIFI_STATE))
        .subscribe {
          granted ->
          if (granted) {
            view.updateYourWifi(wifiManager.currentWifi?.ssid ?: "unknown")
          }
          view.reactOnPermissions(granted)
        }

    compositeDisposable.add(wifiClicksDisposable)

    val statusObs: Observable<Boolean> = Observable.combineLatest(view.radiusChange,
        locationManager.locations,
        locationManager.selectedLocations,
        view.wifiTextChange, Function4 { radius, location, selectedLatLng, wifiName ->
      isInsideGeofence(radius, location, selectedLatLng, wifiName)
    })

    compositeDisposable.add(statusObs.subscribe())
  }

  override fun pause() = compositeDisposable.clear()

  override fun onNewLocationSelected(place: Place?) {

    place?.let {
      locationManager.selectPlace(place)
      view.updateSelectedLocation(place.latLng)
    }
  }

  private fun isInsideGeofence(radius: Int, yourLocation: Location, selectedLatLng: LatLng,
      wifiName: String): Boolean = yourLocation.distanceTo(
      selectedLatLng.location) < radius ||
      wifiManager.currentWifi?.ssid?.contains(wifiName) == true
}