package com.example.mapboxapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapboxMap(
                modifier = Modifier
                    .fillMaxSize(),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        zoom(2.0)
                        center(Point.fromLngLat(-98.0, 39.5))
                        pitch(0.0)
                        bearing(0.0)
                    }
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row (
                    modifier = Modifier
                        .weight(1f)
                ) {}
                Row (
                    modifier = Modifier
                        .weight(0.08f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Green, RoundedCornerShape(8.dp))
                            .border(border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(5.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(5.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Yellow, RoundedCornerShape(8.dp))
                            .border(border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(5.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            }
        }
    }
}