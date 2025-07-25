package com.example.mapboxapp
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.style
import kotlinx.coroutines.launch

public class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MapboxFeatureQuery"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "MainActivity onCreate - Starting app")
        setContent {
            var selectedFeatureName by remember { mutableStateOf<String?>(null) }
            val mapState = rememberMapState()
            val coroutineScope = rememberCoroutineScope()

            Box(modifier = Modifier.fillMaxSize()) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapState = mapState,
                    mapViewportState = rememberMapViewportState {
                        setCameraOptions {
                            zoom(12.0) // Closer zoom for city view
                            center(Point.fromLngLat(12.4964, 41.9028)) // Rome coordinates
                            pitch(0.0)
                            bearing(0.0)
                        }
                    },
                    style = {
                        MapStyle(style = "mapbox://styles/col123/cmdaatylg002301sabcel06cs")
                    },
                    onMapClickListener = { clickedPoint ->
                        Log.i(TAG, "Map clicked at: lat=${clickedPoint.latitude()}, lng=${clickedPoint.longitude()}")

                        coroutineScope.launch {
                            try {
                                Log.i(TAG, "Starting feature query...")

                                // Get screen coordinates
                                val screenCoordinate = mapState.pixelForCoordinate(clickedPoint)
                                Log.i(TAG, "Screen coordinate: x=${screenCoordinate.x}, y=${screenCoordinate.y}")

                                // Query rendered features at the clicked point (all layers)
                                val queryResult = mapState.queryRenderedFeatures(
                                    geometry = RenderedQueryGeometry(screenCoordinate),
                                    options = RenderedQueryOptions(
                                        listOf("poi-label"), // Query all layers
                                        null
                                    )
                                )

                                Log.i(TAG, "Query completed. Checking result...")

                                // Handle the Expected result
                                queryResult.value?.let { queriedRenderedFeatures ->
                                    Log.i(TAG, "Query successful! Found ${queriedRenderedFeatures.size} features")

                                    // Iterate through results to find a feature with a "name" property
                                    for ((index, feature) in queriedRenderedFeatures.withIndex()) {
                                        Log.i(TAG, "Feature $index:")
                                        Log.i(TAG, "  - Layer ID: ${feature.queriedFeature.source}")
                                        Log.i(TAG, "  - Source layer: ${feature.queriedFeature.sourceLayer}")

                                        val properties = feature.queriedFeature.feature.properties()
                                        Log.i(TAG, "  - Properties: $properties")

                                        if (properties != null) {
                                            val propertyKeys = properties.entrySet().map { it.key }
                                            Log.i(TAG, "  - Property keys: $propertyKeys")

                                            if (properties.has("name")) {
                                                val nameProperty = properties.get("name")
                                                Log.i(TAG, "  - Found 'name' property: $nameProperty")

                                                selectedFeatureName = when {
                                                    nameProperty?.isJsonPrimitive == true -> {
                                                        val name = nameProperty.asString
                                                        Log.i(TAG, "  - Extracted name as string: '$name'")
                                                        name
                                                    }
                                                    nameProperty != null -> {
                                                        val name = nameProperty.toString().replace("\"", "")
                                                        Log.i(TAG, "  - Extracted name as toString: '$name'")
                                                        name
                                                    }
                                                    else -> {
                                                        Log.i(TAG, "  - Name property was null")
                                                        null
                                                    }
                                                }

                                                if (selectedFeatureName != null) {
                                                    Log.i(TAG, "SUCCESS: Setting selected feature name to: '$selectedFeatureName'")
                                                    break // Found a feature with name, stop looking
                                                }
                                            } else {
                                                Log.i(TAG, "  - No 'name' property found")

                                                // Log other common name properties to help debug
                                                val commonNameProps = listOf("name", "name_en", "name:en", "title", "label")
                                                for (prop in commonNameProps) {
                                                    if (properties.has(prop)) {
                                                        Log.i(TAG, "  - Found alternative name property '$prop': ${properties.get(prop)}")
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.i(TAG, "  - Properties object was null")
                                        }
                                    }

                                    // If no feature with name found, clear the selection
                                    if (selectedFeatureName == null) {
                                        Log.i(TAG, "No features with 'name' property found. Clearing selection.")
                                    }

                                } ?: run {
                                    // Query failed, clear selection
                                    Log.e(TAG, "Query failed! Error: ${queryResult.error}")
                                    selectedFeatureName = null
                                }

                            } catch (e: Exception) {
                                Log.e(TAG, "Exception during feature query: ${e.message}", e)
                                selectedFeatureName = null
                            }
                        }
                        false
                    }
                )

                // Display box in the center when a feature is selected
                selectedFeatureName?.let { name ->
                    Log.i(TAG, "Displaying feature name box: '$name'")
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .widthIn(min = 200.dp, max = 300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
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