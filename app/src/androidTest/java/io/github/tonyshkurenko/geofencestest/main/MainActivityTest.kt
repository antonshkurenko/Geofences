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

package io.github.tonyshkurenko.geofencestest.main

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import io.github.tonyshkurenko.geofencestest.GeofencesApplication
import io.github.tonyshkurenko.geofencestest.R
import io.github.tonyshkurenko.geofencestest.testutils.DaggerAppComponentTest
import io.github.tonyshkurenko.geofencestest.view.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Project: GeofencesTest
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 * Follow me: @tonyshkurenko

 * @author Anton Shkurenko
 * *
 * @since 7/9/17
 */

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule public val activityTestRule = ActivityTestRule<MainActivity>(
      MainActivity::class.java, false, false)

  private val app = InstrumentationRegistry.getTargetContext().applicationContext as
      GeofencesApplication

  private lateinit var activity: MainActivity

  @Before fun setUp() {
    DaggerAppComponentTest.builder()
        .application(app)
        .build()
        .inject(app)

    activityTestRule.launchActivity(null) // minus @UiThreadTest

    activity = activityTestRule.activity
  }

  @Test fun getLocationClicks() {

    val testObs = activity.locationClicks
        .test()

    onView(withId(R.id.your_location_text_view)).perform(click())
    onView(withId(R.id.your_location_text_view)).perform(click())
    onView(withId(R.id.your_location_text_view)).perform(click())

    testObs.awaitCount(3)
    testObs.assertNotComplete()
    testObs.assertNoErrors()
  }

  @Test fun updateGeofenceStatus() {

    activity.runOnUiThread {
      activity.updateGeofenceStatus(false)
    }

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    onView(withText(app.getString(R.string.main_activity_geofence_status_text,
        false))).check(matches(isDisplayed()))

    activity.runOnUiThread {
      activity.updateGeofenceStatus(true)
    }

    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    onView(withText(app.getString(R.string.main_activity_geofence_status_text,
        true))).check(matches(isDisplayed()))
  }

  // and others ...
}