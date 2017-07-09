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

package io.github.tonyshkurenko.geofencestest.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import io.github.tonyshkurenko.geofencestest.GeofencesApplication
import javax.inject.Singleton

/**
 * Project: GeofencesTest
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 * Follow me: @tonyshkurenko
 *
 * @author Anton Shkurenko
 * @since 7/9/17
 */

@Singleton
@Component(modules = arrayOf(
    AndroidInjectionModule::class,
    AppModule::class,
    ActivityBuilder::class
))
interface AppComponent {

  @Component.Builder
  interface Builder {

    @BindsInstance fun application(application: Application): Builder

    fun build(): AppComponent
  }

  fun inject(app: GeofencesApplication)
}