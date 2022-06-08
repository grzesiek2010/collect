package org.odk.collect.android.geo

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationListener
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayerAt
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.style.sources.Source
import com.mapbox.maps.extension.style.sources.TileSet
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.RasterSource
import com.mapbox.maps.extension.style.sources.generated.VectorSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.odk.collect.android.R
import org.odk.collect.android.geo.MapboxMapConfigurator.MapboxUrlOption
import org.odk.collect.android.geo.mapboxsdk.MapFeature
import org.odk.collect.android.geo.mapboxsdk.MarkerFeature
import org.odk.collect.android.geo.mapboxsdk.PolyFeature
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.location.client.MapboxLocationCallback
import org.odk.collect.android.utilities.ScreenUtils
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragment.ErrorListener
import org.odk.collect.maps.MapFragment.FeatureListener
import org.odk.collect.maps.MapFragment.PointListener
import org.odk.collect.maps.MapFragment.ReadyListener
import org.odk.collect.maps.MapFragmentDelegate
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.layers.MapFragmentReferenceLayerUtils.getReferenceLayerFile
import org.odk.collect.maps.layers.MbtilesFile
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class MapboxMapFragment :
    Fragment(),
    MapFragment,
    OnMapClickListener,
    OnMapLongClickListener,
    LocationListener {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var polylineAnnotationManager: PolylineAnnotationManager

    private var mapReadyListener: ReadyListener? = null
    private val gpsLocationReadyListeners = mutableListOf<ReadyListener>()

    private var nextFeatureId = 1
    private val features = mutableMapOf<Int, MapFeature>()

    private var gpsLocationListener: PointListener? = null
    private var clickListener: PointListener? = null
    private var longPressListener: PointListener? = null

    private var featureClickListener: FeatureListener? = null
    private var featureDragEndListener: FeatureListener? = null

    private var lastLocationProvider: String? = null
    private var lastLocationFix: MapPoint? = null
    private var tileServer: TileHttpServer? = null
    private var referenceLayerFile: File? = null
    private var clientWantsLocationUpdates = false
    private var offlineLayerPosition = -1
    private val locationCallback = MapboxLocationCallback(this)
    private var mapFragmentDelegate: MapFragmentDelegate? = null

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    override fun addTo(
        fragmentManager: FragmentManager,
        containerId: Int,
        readyListener: ReadyListener?,
        errorListener: ErrorListener?
    ) {
        mapReadyListener = readyListener

        // Mapbox SDK only knows how to fetch tiles via HTTP. If we want it to
        // display tiles from a local file, we have to serve them locally over HTTP.
        try {
            tileServer = TileHttpServer().also {
                it.start()
            }
        } catch (e: IOException) {
            Timber.e(e, "Could not start the TileHttpServer")
        }

        // If the containing activity is being re-created upon screen rotation, the FragmentManager
        // will have also re-created a copy of the previous fragment. We don't want these useless
        // copies of old fragments to linger, so the following line calls .replace() instead of .add().
        fragmentManager
            .beginTransaction()
            .replace(containerId, this)
            .commitNow()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mapView = MapView(inflater.context).apply {
            scalebar.enabled = false
            compass.position = Gravity.TOP or Gravity.START
            compass.marginTop = 36f
            compass.marginBottom = 36f
            compass.marginLeft = 36f
            compass.marginRight = 36f
        }

        mapboxMap = mapView
            .getMapboxMap()
            .apply {
                addOnMapClickListener(this@MapboxMapFragment)
                addOnMapLongClickListener(this@MapboxMapFragment)
            }

        polylineAnnotationManager = mapView
            .annotations
            .createPolylineAnnotationManager()

        pointAnnotationManager = mapView
            .annotations
            .createPointAnnotationManager()

        initLocationComponent()
        moveOrAnimateCamera(MapFragment.INITIAL_CENTER, false, MapFragment.INITIAL_ZOOM.toDouble())

        // If the screen is rotated before the map is ready, this fragment could already be detached,
        // which makes it unsafe to use. Only call the ReadyListener if this fragment is still attached.
        if (mapReadyListener != null && activity != null) {
            mapReadyListener!!.onReady(this)
        }

        return mapView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        val configurator = MapboxMapConfigurator(
            KEY_MAPBOX_MAP_STYLE, R.string.basemap_source_mapbox,
            MapboxUrlOption(Style.MAPBOX_STREETS, R.string.streets),
            MapboxUrlOption(Style.LIGHT, R.string.light),
            MapboxUrlOption(Style.DARK, R.string.dark),
            MapboxUrlOption(Style.SATELLITE, R.string.satellite),
            MapboxUrlOption(Style.SATELLITE_STREETS, R.string.hybrid),
            MapboxUrlOption(Style.OUTDOORS, R.string.outdoors)
        )

        mapFragmentDelegate = MapFragmentDelegate(
            configurator,
            settingsProvider.getUnprotectedSettings(),
            this::onConfigChanged
        )
    }

    override fun onStart() {
        super.onStart()
        mapFragmentDelegate?.onStart()
    }

    override fun onResume() {
        super.onResume()
        enableLocationUpdates(clientWantsLocationUpdates)
    }

    override fun onPause() {
        super.onPause()
        enableLocationUpdates(false)
    }

    override fun onStop() {
        mapFragmentDelegate?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        tileServer?.destroy()
        MapsMarkerCache.clearCache()
        super.onDestroy()
    }

    private fun onConfigChanged(config: Bundle) {
        val styleUrl = config.getString(KEY_STYLE_URL) ?: Style.MAPBOX_STREETS
        referenceLayerFile = getReferenceLayerFile(config, referenceLayerRepository)
        mapboxMap.loadStyleUri(styleUrl) {
            if (offlineLayerPosition == -1) {
                // remember the index of first layer above other already existing layers
                offlineLayerPosition = it.styleLayers.size - 1
            }
            loadReferenceOverlay()
        }
    }

    override fun getCenter(): MapPoint {
        val point = mapboxMap.cameraState.center
        return MapPoint(point.latitude(), point.longitude())
    }

    override fun getZoom(): Double {
        return mapboxMap.cameraState.zoom
    }

    override fun setCenter(center: MapPoint?, animate: Boolean) {
        center?.let {
            moveOrAnimateCamera(it, animate)
        }
    }

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
        zoomToPoint(center, MapFragment.POINT_ZOOM.toDouble(), animate)
    }

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
        center?.let {
            moveOrAnimateCamera(it, animate, zoom)
        }
    }

    override fun zoomToBoundingBox(
        mapPoints: Iterable<MapPoint>?,
        scaleFactor: Double,
        animate: Boolean
    ) {
        mapPoints?.let {
            val points = mapPoints.map {
                Point.fromLngLat(it.lon, it.lat, it.alt)
            }

            val screenWidth = ScreenUtils.getScreenWidth()
            val screenHeight = ScreenUtils.getScreenHeight()

            lifecycleScope.launch {
                delay(100L)
                mapboxMap.setCamera(
                    mapboxMap.cameraForCoordinates(
                        points,
                        EdgeInsets(
                            screenHeight / 5.0,
                            screenWidth / 5.0,
                            screenHeight / 5.0,
                            screenWidth / 5.0
                        )
                    )
                )
            }
        }
    }

    override fun addMarker(
        point: MapPoint,
        draggable: Boolean,
        iconAnchor: String,
        iconDrawableId: Int
    ): Int {
        val featureId = nextFeatureId++
        features[featureId] = MarkerFeature(
            requireContext(),
            pointAnnotationManager,
            featureId,
            featureClickListener,
            featureDragEndListener,
            point,
            draggable,
            iconAnchor,
            iconDrawableId
        )
        return featureId
    }

    override fun setMarkerIcon(featureId: Int, drawableId: Int) {
        val feature = features[featureId]
        if (feature is MarkerFeature) {
            feature.setIcon(drawableId)
        }
    }

    override fun getMarkerPoint(featureId: Int): MapPoint? {
        val feature = features[featureId]
        return if (feature is MarkerFeature) {
            feature.point
        } else {
            null
        }
    }

    override fun addDraggablePoly(points: MutableIterable<MapPoint>, closedPolygon: Boolean): Int {
        val featureId = nextFeatureId++
        features[featureId] = PolyFeature(
            requireContext(),
            pointAnnotationManager,
            polylineAnnotationManager,
            featureId,
            featureClickListener,
            featureDragEndListener,
            closedPolygon,
            points
        )
        return featureId
    }

    override fun appendPointToPoly(featureId: Int, point: MapPoint) {
        val feature = features[featureId]
        if (feature is PolyFeature) {
            feature.appendPoint(point)
        }
    }

    override fun removePolyLastPoint(featureId: Int) {
        val feature = features[featureId]
        if (feature is PolyFeature) {
            feature.removeLastPoint()
        }
    }

    override fun getPolyPoints(featureId: Int): List<MapPoint> {
        val feature = features[featureId]
        return if (feature is PolyFeature) {
            feature.mapPoints
        } else {
            emptyList()
        }
    }

    override fun clearFeatures() {
        for (feature in features.values) {
            feature.dispose()
        }

        features.clear()
        nextFeatureId = 1
    }

    override fun setClickListener(listener: PointListener?) {
        clickListener = listener
    }

    override fun setLongPressListener(listener: PointListener?) {
        longPressListener = listener
    }

    override fun setFeatureClickListener(listener: FeatureListener?) {
        featureClickListener = listener
    }

    override fun setDragEndListener(listener: FeatureListener?) {
        featureDragEndListener = listener
    }

    override fun setGpsLocationEnabled(enabled: Boolean) {
        if (enabled != clientWantsLocationUpdates) {
            clientWantsLocationUpdates = enabled
            enableLocationUpdates(clientWantsLocationUpdates)
        }
    }

    override fun getGpsLocation(): MapPoint? {
        return lastLocationFix
    }

    override fun getLocationProvider(): String? {
        return lastLocationProvider
    }

    override fun runOnGpsLocationReady(listener: ReadyListener) {
        if (lastLocationFix != null) {
            listener.onReady(this)
        } else {
            gpsLocationReadyListeners.add(listener)
        }
    }

    override fun setGpsLocationListener(listener: PointListener?) {
        gpsLocationListener = listener
    }

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
        locationCallback.setRetainMockAccuracy(retainMockAccuracy)
    }

    override fun onMapClick(point: Point): Boolean {
        clickListener?.onPoint(MapPoint(point.latitude(), point.longitude()))

        // MAPBOX ISSUE: Unfortunately, onMapClick is called before onAnnotationClick,
        // which means that every click on a marker will also cause a click event on
        // the map. Returning true will consume the event and prevent the marker's
        // onAnnotationClick from ever being called, so we have to return false.
        return false
    }

    override fun onMapLongClick(point: Point): Boolean {
        longPressListener?.onPoint(MapPoint(point.latitude(), point.longitude()))
        return true
    }

    override fun onLocationChanged(location: Location) {
        lastLocationFix = MapPoint(
            location.latitude, location.longitude,
            location.altitude, location.accuracy.toDouble()
        )
        lastLocationProvider = location.provider
        Timber.i(
            "Received location update: %s (%s)",
            lastLocationFix,
            lastLocationProvider
        )
        for (listener in gpsLocationReadyListeners) {
            listener.onReady(this)
        }
        gpsLocationReadyListeners.clear()
        gpsLocationListener?.onPoint(lastLocationFix!!)
    }

    @SuppressWarnings("MissingPermission") // permission checks for location services are handled in widgets
    private fun enableLocationUpdates(enabled: Boolean) {
        val engine = LocationEngineProvider.getBestLocationEngine(requireContext())
        if (enabled) {
            Timber.i("Requesting location updates from %s (to %s)", engine, this)
            engine.requestLocationUpdates(
                LocationEngineRequest.Builder(1000)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(5000)
                    .build(),
                locationCallback,
                null
            )
            engine.getLastLocation(locationCallback)
        } else {
            Timber.i("Stopping location updates from %s (to %s)", engine, this)
            engine.removeLocationUpdates(locationCallback)
        }
        mapView.location.enabled = enabled
    }

    private fun initLocationComponent() {
        mapView.location.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_crosshairs,
                )
            )
        }
    }

    private fun moveOrAnimateCamera(point: MapPoint, animate: Boolean, zoom: Double = getZoom()) {
        mapboxMap.flyTo(
            cameraOptions {
                center(Point.fromLngLat(point.lon, point.lat, point.alt))
                zoom(zoom)
            },
            mapAnimationOptions {
                duration(if (animate) 300 else 0)
            }
        )
    }

    private fun loadReferenceOverlay() {
        mapboxMap.getStyle()?.removeStyleLayer(OFFLINE_LAYER_ID)
        referenceLayerFile?.let {
            addMbtiles(it.name, referenceLayerFile!!)
        }
    }

    private fun addMbtiles(id: String, file: File) {
        tileServer?.let {
            val mbtiles: MbtilesFile = try {
                MbtilesFile(file)
            } catch (e: MbtilesFile.MbtilesException) {
                Timber.w(e.message)
                return
            }

            val tileSet = createTileSet(mbtiles, it.getUrlTemplate(id))
            it.addSource(id, mbtiles)

            if (mbtiles.layerType == MbtilesFile.LayerType.VECTOR) {
                addOverlaySource(VectorSource.Builder(id).tileSet(tileSet).build())
                val layers = mbtiles.vectorLayers
                for (layer in layers) {
                    // Pick a colour that's a function of the filename and layer name.
                    // The colour will appear essentially random; the only purpose here
                    // is to try to assign different colours to different layers, such
                    // that each individual layer appears in its own consistent colour.
                    val hue = ((id + "." + layer.name).hashCode() and 0x7fffffff) % 360
                    addOverlayLayer(
                        LineLayer(OFFLINE_LAYER_ID, id)
                            .lineColor(Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 1f)))
                            .lineWidth(1.0)
                            .lineOpacity(0.7)
                            .sourceLayer(layer.name)
                    )
                }
            }
            if (mbtiles.layerType == MbtilesFile.LayerType.RASTER) {
                addOverlaySource(RasterSource.Builder(id).tileSet(tileSet).build())
                addOverlayLayer(RasterLayer(OFFLINE_LAYER_ID, id))
            }
            Timber.i("Added %s as a %s layer at /%s", file, mbtiles.layerType, id)
        }
    }

    private fun createTileSet(mbtiles: MbtilesFile, urlTemplate: String): TileSet {
        val tileSet = TileSet.Builder("2.2.0", listOf(urlTemplate))

        // Configure the TileSet using the metadata in the .mbtiles file.
        try {
            tileSet.name(mbtiles.getMetadata("name"))
            try {
                tileSet.minZoom(mbtiles.getMetadata("minzoom").toInt())
                tileSet.maxZoom(mbtiles.getMetadata("maxzoom").toInt())
            } catch (e: NumberFormatException) {
                /* ignore */
            }
            var parts = mbtiles.getMetadata("center").split(",").toTypedArray()
            if (parts.size == 3) { // latitude, longitude, zoom
                try {
                    tileSet.center(
                        listOf(
                            parts[0].toDouble(),
                            parts[1].toDouble(),
                            parts[2].toDouble()
                        )
                    )
                } catch (e: NumberFormatException) {
                    /* ignore */
                }
            }
            parts = mbtiles.getMetadata("bounds").split(",").toTypedArray()
            if (parts.size == 4) { // left, bottom, right, top
                try {
                    tileSet.bounds(
                        listOf(
                            parts[0].toDouble(),
                            parts[1].toDouble(),
                            parts[2].toDouble(),
                            parts[3].toDouble()
                        )
                    )
                } catch (e: NumberFormatException) {
                    /* ignore */
                }
            }
        } catch (e: MbtilesFile.MbtilesException) {
            Timber.w(e.message)
        }
        return tileSet.build()
    }

    private fun addOverlayLayer(layer: Layer) {
        mapboxMap.getStyle()?.addLayerAt(layer, offlineLayerPosition)
    }

    private fun addOverlaySource(source: Source) {
        if (mapboxMap.getStyle()?.getSource(source.sourceId) == null) {
            mapboxMap.getStyle()?.addSource(source)
        }
    }

    companion object {
        const val KEY_STYLE_URL = "STYLE_URL"
        const val OFFLINE_LAYER_ID = "offline_layer_id"
    }
}
