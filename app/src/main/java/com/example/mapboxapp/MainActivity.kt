package com.example.mapboxapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.SourceQueryOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MapboxFeatureQuery"
    }

    private fun distanceBetween(p1: Point, p2: Point): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude(), p1.longitude(),
            p2.latitude(), p2.longitude(),
            results
        )
        return results[0].toDouble()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var selectedFeatureName by remember { mutableStateOf<String?>(null) }
            val mapState = rememberMapState()
            val coroutineScope = rememberCoroutineScope()

            Box(modifier = Modifier.fillMaxSize()) {
                val mapboxMapRef = remember { mutableStateOf<com.mapbox.maps.MapboxMap?>(null) }
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapState = mapState,
                    mapViewportState = rememberMapViewportState {
                        setCameraOptions {
                            zoom(12.0)
                            center(Point.fromLngLat(12.4964, 41.9028)) // Rome
                        }
                    },
                    style = {
                        MapStyle("mapbox://styles/col123/cmdaatylg002301sabcel06cs")
                    },
                    onMapClickListener = { clickedPoint ->
                        coroutineScope.launch {
                            val mapboxMap = mapboxMapRef.value
                            if (mapboxMap == null) {
                                Log.i(TAG, "MapboxMap reference not initialized yet")
                                return@launch
                            }
                            try {
                                mapboxMap.querySourceFeatures(
                                    sourceId = "composite",
                                    options = SourceQueryOptions(
                                        listOf("poi_label"),
                                        Value.valueOf(true) // no filter - query all features in layer
                                    )
                                ) { result ->
                                    if (result.isValue) {
                                        val features = result.value
                                        var closestFeature: Feature? = null
                                        var closestDistance = Double.MAX_VALUE

                                        features?.forEach { featureWithMetadata ->
                                            val feature = featureWithMetadata.queriedFeature.feature
                                            val geometry = feature.geometry()
                                            if (geometry is Point) {
                                                val distance = distanceBetween(clickedPoint, geometry)
                                                if (distance < closestDistance && feature.hasProperty("name")) {
                                                    closestDistance = distance
                                                    closestFeature = feature
                                                }
                                            }
                                        }
                                        selectedFeatureName = closestFeature?.getStringProperty("name")
                                        Log.i(TAG, "Selected closest feature: $selectedFeatureName (distance: $closestDistance meters)")
                                    } else {
                                        Log.i(TAG, "querySourceFeatures error: ${result.error}")
                                    }
                                }
                            } catch (ex: Exception) {
                                Log.i(TAG, "Exception querying source features: ${ex.message}", ex)
                            }
                        }
                        false // Return false to indicate event not consumed
                    }
                ) {
                    // **This is key:** place MapEffect *inside* the MapboxMap scope!
                    MapEffect { mapView ->
                        mapboxMapRef.value = mapView.mapboxMap
                    }

//                    MapEffect(mapState) { mapView ->
//                        val mapboxMap = mapView.mapboxMap
//                        mapboxMap.getStyle { style ->
//                            val sources = style.styleSources
//                            Log.i(TAG, "Sources in style: ${sources.first()}")
//                        }
//                        mapboxMapRef.value = mapboxMap
//                    }

                }
                // UI to show selected feature name
                selectedFeatureName?.let { name ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .widthIn(min = 200.dp, max = 300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Feature Name",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap map to dismiss",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
