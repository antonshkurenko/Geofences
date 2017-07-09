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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.github.tonyshkurenko.geofencestest.R
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

  @Inject
  lateinit var presenter: MainPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }

  override fun onResume() {
    super.onResume()

    presenter.resume()
  }

  override fun onPause() {
    super.onPause()

    presenter.pause()
  }

}