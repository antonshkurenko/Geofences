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

package io.github.tonyshkurenko.geofencestest.testutils

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.wifi.WifiInfo
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import dagger.Module
import dagger.Provides
import io.github.tonyshkurenko.geofencestest.model.LocationManager
import io.github.tonyshkurenko.geofencestest.model.WifiManager
import io.reactivex.Observable
import javax.inject.Singleton


/**
 * Project: GeofencesTest
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 * Follow me: @tonyshkurenko
 *
 * @author Anton Shkurenko
 * @since 7/9/17
 */
@Module class AppModuleTest /*: AppModule*/() {

  @Provides
  @Singleton
  fun context(application: Application): Context {
    return application
  }

  @Provides
  @Singleton
  fun locationManager(): LocationManager {
    return StubLocationManager()
  }

  @Provides
  @Singleton
  fun wifiManager(): WifiManager {
    return StubWifiManager()
  }

  class StubLocationManager : LocationManager {
    override val selectedLocations: Observable<LatLng>
      get() = Observable.fromArray(LatLng(0.0, 0.0), LatLng(0.0, 0.0), LatLng(0.0, 0.0))

    override fun selectPlace(place: Place) {
      // nothing to do
    }

    override fun connect() {
      // nothing to do
    }

    override val locations: Observable<Location> = Observable.fromArray(Location("fake"),
        Location("fake"), Location("fake"))
  }

  class StubWifiManager : WifiManager {
    override val wifi: Observable<WifiInfo> = Observable.never()
  }
}