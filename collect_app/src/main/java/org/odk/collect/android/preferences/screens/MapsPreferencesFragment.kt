/*
 * Copyright (C) 2017 Shobhit
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
package org.odk.collect.android.preferences.screens

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import org.odk.collect.android.R
import org.odk.collect.android.geo.MapConfiguratorProvider
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.PrefUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.OfflineMapLayersPicker
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.keys.ProjectKeys.CATEGORY_BASEMAP
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.webpage.ExternalWebPageHelper
import javax.inject.Inject

class MapsPreferencesFragment : BaseProjectPreferencesFragment(), Preference.OnPreferenceClickListener {

    private lateinit var basemapSourcePref: ListPreference

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var externalWebPageHelper: ExternalWebPageHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPicker::class) {
                OfflineMapLayersPicker(referenceLayerRepository, scheduler, settingsProvider, externalWebPageHelper)
            }
            .build()

        super.onCreate(savedInstanceState)
    }

    override fun onSettingChanged(key: String) {
        super.onSettingChanged(key)
        if (key == REFERENCE_LAYER_KEY) {
            findPreference<Preference>(REFERENCE_LAYER_KEY)!!.summary = getLayerName()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.maps_preferences, rootKey)
        initBasemapSourcePref()
        initLayersPref()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (allowClick(javaClass.name)) {
            when (preference.key) {
                REFERENCE_LAYER_KEY -> {
                    DialogFragmentUtils.showIfNotShowing(
                        OfflineMapLayersPicker::class.java,
                        childFragmentManager
                    )
                }
            }
            return true
        }
        return false
    }

    /**
     * Creates the Basemap Source preference widget (but doesn't add it to
     * the screen; onBasemapSourceChanged will do that part).
     */
    private fun initBasemapSourcePref() {
        basemapSourcePref = PrefUtils.createListPref(
            requireContext(),
            KEY_BASEMAP_SOURCE,
            getString(org.odk.collect.strings.R.string.basemap_source),
            MapConfiguratorProvider.getLabelIds(),
            MapConfiguratorProvider.getIds(),
            settingsProvider.getUnprotectedSettings()
        )

        basemapSourcePref.setIconSpaceReserved(false)
        onBasemapSourceChanged(MapConfiguratorProvider.getConfigurator())
        basemapSourcePref.setOnPreferenceChangeListener { _: Preference?, value: Any ->
            val cftor = MapConfiguratorProvider.getConfigurator(value.toString())
            if (!cftor.isAvailable(context)) {
                cftor.showUnavailableMessage(context)
                false
            } else {
                onBasemapSourceChanged(cftor)
                true
            }
        }
    }

    private fun initLayersPref() {
        findPreference<Preference>(REFERENCE_LAYER_KEY)?.apply {
            onPreferenceClickListener = this@MapsPreferencesFragment
            summary = getLayerName()
        }
    }

    private fun getLayerName(): String {
        val layerId = settingsProvider.getUnprotectedSettings().getString(REFERENCE_LAYER_KEY)
        return if (layerId == null) {
            requireContext().getLocalizedString(org.odk.collect.strings.R.string.none)
        } else {
            referenceLayerRepository.get(layerId)!!.name
        }
    }

    /** Updates the rest of the preference UI when the Basemap Source is changed.  */
    private fun onBasemapSourceChanged(cftor: MapConfigurator) {
        // Set up the preferences in the "Basemap" section.
        val baseCategory = findPreference<PreferenceCategory>(CATEGORY_BASEMAP)
        baseCategory!!.removeAll()
        baseCategory.addPreference(basemapSourcePref)
        for (pref in cftor.createPrefs(context, settingsProvider.getUnprotectedSettings())) {
            pref.isIconSpaceReserved = false
            baseCategory.addPreference(pref)
        }

        // Clear the reference layer if it isn't supported by the new basemap.
        val layerId = settingsProvider.getUnprotectedSettings().getString(REFERENCE_LAYER_KEY)
        if (layerId != null && !cftor.supportsLayer(referenceLayerRepository.get(layerId)!!.file)) {
            settingsProvider.getUnprotectedSettings().save(REFERENCE_LAYER_KEY, null)
        }
    }

    companion object {
        const val REFERENCE_LAYER_KEY = "reference_layer"
    }
}
