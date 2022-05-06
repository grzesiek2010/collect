package org.odk.collect.android.geo.mapboxsdk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap

/**
 * This is an exact copy of com.mapbox.maps.testapp.examples.fragment as of tag
 * android-v10.5.0, with only these changes for compatibility with ODK Collect:
 *   - "open" is added, so this class can be extended.
 *
 * Fragment wrapper around a map view.
 * <p>
 * A Map component in an app. This fragment is the simplest way to place a map in an application.
 * It's a wrapper around a view of a map to automatically handle the necessary life cycle needs.
 * Being a fragment, this component can be added to an activity's layout or can dynamically be added
 * using a FragmentManager.
 * </p>
 * <p>
 * To get a reference to the MapView, use {@link #getMapAsync(callback: (MapboxMap) -> Unit)}}
 * </p>
 *
 * @see #getMapAsync(callback: (MapboxMap) -> Unit)
 */
open class MapFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var onMapReady: (MapboxMap) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mapView = MapView(
            inflater.context,
            MapInitOptions(inflater.context)
        )
        return mapView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapboxMap = mapView.getMapboxMap()
        if (::onMapReady.isInitialized) {
            onMapReady.invoke(mapboxMap)
        }
    }

    fun getMapAsync(callback: (MapboxMap) -> Unit) = if (::mapboxMap.isInitialized) {
        callback.invoke(mapboxMap)
    } else this.onMapReady = callback

    fun getMapView(): MapView {
        return mapView
    }
}
