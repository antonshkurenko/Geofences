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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import io.reactivex.Observable
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
@Singleton class WifiManagerImpl @Inject constructor(val context: Context) : WifiManager {

  override val wifi: Observable<WifiInfo>
    get() {
      return Observable.create<WifiInfo> {
        emitter ->

        val receiver = object : BroadcastReceiver() {
          override fun onReceive(context: Context?, intent: Intent?) {
            emitter.onNext(wifiManager.connectionInfo)
          }
        }

        context.registerReceiver(receiver, IntentFilter("android.net.wifi.STATE_CHANGE"))

        emitter.setCancellable { context.unregisterReceiver(receiver) }
      }.startWith(wifiManager.connectionInfo)
    }

  private val wifiManager by lazy {
    context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
  }

}