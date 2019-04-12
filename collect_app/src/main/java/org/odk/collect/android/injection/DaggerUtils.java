/*
 * Copyright (C) 2018 Callum Stott
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.injection;

import android.support.v7.app.AppCompatActivity;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyComponent;

public class DaggerUtils {

    private DaggerUtils() {}

    public static AppDependencyComponent getComponent(AppCompatActivity activity) {
        return ((Collect) activity.getApplication()).getComponent();
    }
}
