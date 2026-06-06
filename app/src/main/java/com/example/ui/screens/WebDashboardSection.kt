package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.PropertyEntity

@Composable
fun WebDashboardSection(properties: List<PropertyEntity>) {
    // Generate JavaScript properties serialized array securely
    val jsProperties = properties.map { prop ->
        val firstImage = prop.imageUrls.split(",").firstOrNull()?.trim() ?: ""
        """
        {
            title: "${prop.title.replace("\"", "\\\"").replace("\n", " ")}",
            city: "${prop.city.replace("\"", "\\\"")}",
            address: "${prop.address.replace("\"", "\\\"")}",
            price: ${prop.price},
            rating: ${prop.rating},
            type: "${prop.propertyType}",
            image: "$firstImage",
            lat: ${prop.latitude},
            lng: ${prop.longitude}
        }
        """.trimIndent()
    }.joinToString(",")

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <title>Zellige Dashboard Web Map</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    background: #f7f5f0;
                }
                #map {
                    height: 100%;
                    width: 100%;
                }
                .filter-header {
                    position: absolute;
                    top: 12px;
                    left: 12px;
                    z-index: 1000;
                    background: rgba(26, 35, 126, 0.95);
                    padding: 10px 16px;
                    border-radius: 12px;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.25);
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    border: 1px solid #bfa054;
                }
                .filter-header label {
                    color: #fff;
                    font-weight: bold;
                    font-size: 13px;
                    letter-spacing: 0.5px;
                }
                .filter-header select {
                    background: #fff;
                    font-size: 13px;
                    padding: 6px 10px;
                    border-radius: 6px;
                    border: none;
                    font-weight: bold;
                    color: #1a237e;
                    outline: none;
                    cursor: pointer;
                }
                .popup-card {
                    width: 160px;
                    display: flex;
                    flex-direction: column;
                }
                .popup-card img {
                    width: 100%;
                    height: 85px;
                    object-fit: cover;
                    border-radius: 6px;
                    margin-bottom: 6px;
                }
                .popup-title {
                    font-weight: bold;
                    margin: 2px 0;
                    font-size: 12px;
                    color: #1a237e;
                    line-height: 1.3;
                }
                .popup-price {
                    color: #bfa054;
                    font-weight: 800;
                    font-size: 12px;
                    margin-top: 4px;
                }
            </style>
        </head>
        <body>
            <div class="filter-header">
                <label for="citySelect">Filter by City:</label>
                <select id="citySelect" onchange="filterMap(this.value)">
                    <option value="all">All Moroccan Properties</option>
                    <option value="Marrakech">Marrakech</option>
                    <option value="Casablanca">Casablanca</option>
                    <option value="Rabat">Rabat</option>
                    <option value="Agadir">Agadir</option>
                    <option value="Tangier">Tangier</option>
                    <option value="Chefchaouen">Chefchaouen</option>
                </select>
            </div>
            
            <div id="map"></div>

            <script>
                // Instantiating beautiful central Leaflet map
                var map = L.map('map', { zoomControl: false }).setView([31.7917, -7.0926], 6);
                
                L.control.zoom({ position: 'bottomright' }).addTo(map);

                // OpenStreetMap Tile Layer
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);

                // Static + Dynamic Properties list
                var properties = [$jsProperties];
                var markersGroup = L.layerGroup().addTo(map);

                function loadPins(cityFilter) {
                    markersGroup.clearLayers();
                    var bounds = [];

                    properties.forEach(function(p) {
                        if (cityFilter !== "all" && p.city.toLowerCase() !== cityFilter.toLowerCase()) {
                            return;
                        }

                        // Ignore unset coordinates
                        if (p.lat === 0 && p.lng === 0) return;

                        var popupHtml = '<div class="popup-card">' +
                            (p.image ? '<img src="' + p.image + '" />' : '') +
                            '<div class="popup-title">' + p.title + '</div>' +
                            '<div style="font-size:10px; color:#666;">' + p.type + ' • ' + p.city + '</div>' +
                            '<div class="popup-price">&#36;' + p.price + ' / night</div>' +
                            '</div>';

                        var m = L.marker([p.lat, p.lng]).bindPopup(popupHtml);
                        markersGroup.addLayer(m);
                        bounds.push([p.lat, p.lng]);
                    });

                    if (bounds.length > 0) {
                        map.fitBounds(bounds, { padding: [40, 40] });
                    }
                }

                function filterMap(selectedCity) {
                    loadPins(selectedCity);
                }

                // Fire initial trigger
                loadPins("all");
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = Modifier.fillMaxSize()
    )
}
