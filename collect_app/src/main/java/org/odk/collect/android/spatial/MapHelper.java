/*
 * Copyright (C) 2015 Nafundi
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

package org.odk.collect.android.spatial;

/**
 * Created by jnordling on 12/29/15.
 *
 * @author jonnordling@gmail.com
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class MapHelper {
    public static String[] offilineOverlays;

    public GoogleMap googleMap;
    public MapView osmMap;

    // GOOGLE MAPS BASEMAPS
    private static final String GOOGLE_MAP_STREETS = "streets";
    private static final String GOOGLE_MAP_SATELLITE = "satellite";
    private static final String GOOGLE_MAP_TERRAIN = "terrain‎";
    private static final String GOOGLE_MAP_HYBRID = "hybrid";

    //OSM MAP BASEMAPS
    private static final String OPENMAP_STREETS = "openmap_streets";
    private static final String OPENMAP_USGS_TOPO = "openmap_usgs_topo";
    private static final String OPENMAP_USGS_SAT = "openmap_usgs_sat";
    private static final String OPENMAP_STAMEN_TERRAIN = "openmap_stamen_terrain";
    private static final String OPENMAP_CARTODB_POSITRON = "openmap_cartodb_positron";
    private static final String OPENMAP_CARTODB_DARKMATTER = "openmap_cartodb_darkmatter";
    private int selectedLayer = 0;

    private IRegisterReceiver iregisterReceiver;

    private String basemap;

    private org.odk.collect.android.spatial.TileSourceFactory tileFactory;

    public MapHelper(Context context, GoogleMap googleMap, String basemap) {
        this.googleMap = null;
        osmMap = null;
        offilineOverlays = getOfflineLayerList(context);
        this.googleMap = googleMap;
        tileFactory = new org.odk.collect.android.spatial.TileSourceFactory(context);
        this.basemap = basemap;
    }

    public MapHelper(Context context, MapView osmMap, IRegisterReceiver iregisterReceiver, String basemap) {
        googleMap = null;
        this.osmMap = null;
        offilineOverlays = getOfflineLayerList(context);
        this.iregisterReceiver = iregisterReceiver;
        this.osmMap = osmMap;
        tileFactory = new org.odk.collect.android.spatial.TileSourceFactory(context);
        this.basemap = basemap;
    }

    public static String getGoogleBasemap(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.KEY_MAP_BASEMAP, GOOGLE_MAP_STREETS);
    }

    public static String getOsmBasemap(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.KEY_MAP_BASEMAP, OPENMAP_STREETS);
    }

    public void setBasemap() {
        if (googleMap != null) {
            switch (basemap) {
                case GOOGLE_MAP_STREETS:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case GOOGLE_MAP_SATELLITE:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case GOOGLE_MAP_TERRAIN:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case GOOGLE_MAP_HYBRID:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                default:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
            }
        } else {
            //OSMMAP
            ITileSource tileSource;

            switch (basemap) {
                case OPENMAP_USGS_TOPO:
                    tileSource = tileFactory.getUSGSTopo();
                    break;

                case OPENMAP_USGS_SAT:
                    tileSource = tileFactory.getUsgsSat();
                    break;

                case OPENMAP_STAMEN_TERRAIN:
                    tileSource = tileFactory.getStamenTerrain();
                    break;

                case OPENMAP_CARTODB_POSITRON:
                    tileSource = tileFactory.getCartoDbPositron();
                    break;

                case OPENMAP_CARTODB_DARKMATTER:
                    tileSource = tileFactory.getCartoDbDarkMatter();
                    break;

                case OPENMAP_STREETS:
                default:
                    tileSource = TileSourceFactory.MAPNIK;
                    break;
            }

            if (tileSource != null) {
                osmMap.setTileSource(tileSource);
            }
        }
    }

    private String[] getOfflineLayerList(Context context) {
        if (googleMap != null) {
            return context.getResources().getStringArray(R.array.map_google_basemap_selector_entry_values);
        } else {
            return context.getResources().getStringArray(R.array.map_osm_basemap_selector_entry_values);
        }
    }

    public void showLayersDialog(final Context context) {
        AlertDialog.Builder layerDialod = new AlertDialog.Builder(context);
        layerDialod.setTitle(context.getString(R.string.select_offline_layer));
        layerDialod.setSingleChoiceItems(offilineOverlays,
                selectedLayer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (googleMap != null) {
                            basemap = context.getResources().getStringArray(R.array.map_google_basemap_selector_entry_values)[item];
                        } else {
                            osmMap.invalidate();
                            basemap = context.getResources().getStringArray(R.array.map_osm_basemap_selector_entry_values)[item];
                        }
                        selectedLayer = item;
                        setBasemap();
                        dialog.dismiss();
                    }
                });
        layerDialod.show();
    }
}
