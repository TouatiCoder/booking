package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

@Composable
fun OSMMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    isEditable: Boolean = false,
    onCoordinatesChanged: ((Double, Double) -> Unit)? = null
) {
    var mapViewState by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                // Required user agent configuration for OSM server tiles compliance
                Configuration.getInstance().userAgentValue = context.packageName
                
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                    controller.setZoom(15.0)
                    
                    val centerPoint = GeoPoint(latitude, longitude)
                    controller.setCenter(centerPoint)
                    
                    // Display initial marker Pin
                    val marker = Marker(this).apply {
                        position = centerPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Selected Location"
                    }
                    overlays.add(marker)

                    // Touch events listener for coordinates picking
                    if (isEditable && onCoordinatesChanged != null) {
                        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                marker.position = p
                                invalidate()
                                onCoordinatesChanged(p.latitude, p.longitude)
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean {
                                marker.position = p
                                invalidate()
                                onCoordinatesChanged(p.latitude, p.longitude)
                                return true
                            }
                        })
                        overlays.add(eventsOverlay)
                    }
                }
            },
            update = { mapView ->
                val targetPoint = GeoPoint(latitude, longitude)
                mapView.controller.setCenter(targetPoint)
                val marker = mapView.overlays.filterIsInstance<Marker>().firstOrNull()
                if (marker != null) {
                    marker.position = targetPoint
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay showing GPS coordinates for precision reference
        Card(
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.LuxuryDarkBlue.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = "GPS: ${String.format("%.5f", latitude)}, ${String.format("%.5f", longitude)}",
                    fontSize = 11.sp,
                    color = com.example.ui.theme.LuxuryGold,
                    style = MaterialTheme.typography.bodySmall
                )
                if (isEditable) {
                    Text(
                        text = "Tap on map to pins location",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
