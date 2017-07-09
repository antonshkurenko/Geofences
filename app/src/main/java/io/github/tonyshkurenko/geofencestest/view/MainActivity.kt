package io.github.tonyshkurenko.geofencestest.view

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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import dagger.android.AndroidInjection
import io.github.tonyshkurenko.geofencestest.R
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

  companion object {
    const val REQUEST_LOCATION = 1
  }

  @Inject
  lateinit var presenter: MainPresenter

  override val locationClicks: Observable<View>
    get() = Observable.create { emitter ->
      selectedLocationTextView.setOnClickListener { emitter.onNext(it) }
    }

  override val wifiTextChange: Observable<String>
    get() = Observable.create { emitter ->
      wifiEditText.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
          if (s == null) return // should never happen
          emitter.onNext(s.toString())
        }
      })
    }

  override val radiusChange: Observable<Int>
    get() = Observable.create { emitter ->
      radiusEditText.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
          if (s == null) return // should never happen
          try {
            emitter.onNext(s.toString().toInt())
          } catch (e: NumberFormatException) {
            emitter.onNext(0)
          }
        }
      })
    }

  //region Views
  internal lateinit var statusTextView: TextView
  internal lateinit var yourLocationTextView: TextView
  internal lateinit var yourWifiTextView: TextView
  internal lateinit var selectedLocationTextView: TextView
  internal lateinit var distanceBetweenTextView: TextView
  internal lateinit var radiusEditText: EditText
  internal lateinit var wifiEditText: EditText
  //endregion

  //region Extends Activity
  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    statusTextView = findViewById(R.id.status_text_view) as TextView
    yourLocationTextView = findViewById(R.id.your_location_text_view) as TextView
    yourWifiTextView = findViewById(R.id.your_wifi_text_view) as TextView
    selectedLocationTextView = findViewById(R.id.selected_location_text_view) as TextView
    distanceBetweenTextView = findViewById(R.id.distance_between_text_view) as TextView
    radiusEditText = findViewById(R.id.radius_input_layout_edit_text) as EditText
    wifiEditText = findViewById(R.id.wifi_network_edit_text) as EditText

    presenter.create()
  }

  override fun onResume() {
    super.onResume()

    presenter.resume()
  }

  override fun onPause() {
    super.onPause()

    presenter.pause()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode != Activity.RESULT_OK) {
      Timber.d("Result isn't ok, canceling")
      return
    }

    when (requestCode) {
      REQUEST_LOCATION -> presenter.onNewLocationSelected(PlacePicker.getPlace(this, data))
    }
  }
  //endregion

  //region Implements MainView
  override fun updateGeofenceStatus(inside: Boolean) {
    statusTextView.text = getString(R.string.main_activity_geofence_status_text, inside)
  }

  override fun updateYourLocation(latlng: LatLng?) {
    yourLocationTextView.text = getString(R.string.main_activity_your_location_text,
        latlng.toString())
  }

  override fun updateDistanceBetween(distance: Float) {
    distanceBetweenTextView.text = getString(R.string.main_activity_distance_between_text, distance)
  }

  override fun updateYourWifi(yourWifiName: String) {
    yourWifiTextView.text = getString(R.string.main_activity_your_wifi_text, yourWifiName)
  }

  override fun updateSelectedLocation(latlng: LatLng) {
    selectedLocationTextView.text = getString(R.string.main_activity_selected_location_text,
        latlng.toString())
  }

  override fun selectNewLocation() =
      startActivityForResult(PlacePicker.IntentBuilder().build(this),
          REQUEST_LOCATION)

  override fun reactOnPermissions(granted: Boolean) =
      Toast.makeText(this, "You granted permission: $granted", Toast.LENGTH_SHORT).show()

  override fun close() = finish()
  //endregion
}
