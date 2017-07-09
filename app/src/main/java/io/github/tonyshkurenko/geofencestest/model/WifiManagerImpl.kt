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

import android.content.Context
import android.net.wifi.WifiInfo
import javax.inject.Inject
import javax.inject.Singleton
import android.net.NetworkInfo.DetailedState



/**
 * Project: GeofencesTest
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 * Follow me: @tonyshkurenko
 *
 * @author Anton Shkurenko
 * @since 7/9/17
 */
@Singleton class WifiManagerImpl @Inject constructor(context: Context) : WifiManager {

  override val currentWifi: WifiInfo?
    get() {

      val wifiInfo = wifiManager.connectionInfo
      if (wifiInfo != null) {
        val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
        if (state == DetailedState.CONNECTED || state == DetailedState.OBTAINING_IPADDR) {
          return wifiInfo
        }
      }
      return null
    }

  private val wifiManager by lazy {
    context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
  }
}