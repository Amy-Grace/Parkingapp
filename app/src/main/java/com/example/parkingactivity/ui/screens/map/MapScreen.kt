package com.example.parkingactivity.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkingactivity.data.ParkingFacility
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onFacilityClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val facilities by viewModel.facilities.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            viewModel.getCurrentLocation()
        }
    }
    
    // Check location permission
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.getCurrentLocation()
        }
    }
    
    // Cleanup resources when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Parking") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.getCurrentLocation() }) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "My Location"
                )
            }
        }
    ) { paddingValues ->
        if (!hasLocationPermission) {
            // Show request permission UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Location permission is required to show nearby parking facilities.",
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            // Show Mapbox map
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Using AndroidView to integrate MapBox
                AndroidView(
                    factory = { context ->
                        MapView(context).also { mapView ->
                            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                            
                            // Set initial camera position
                            userLocation?.let { location ->
                                val point = Point.fromLngLat(location.longitude, location.latitude)
                                mapView.getMapboxMap().setCamera(
                                    CameraOptions.Builder()
                                        .center(point)
                                        .zoom(15.0)
                                        .build()
                                )
                                
                                // Add user location marker
                                val annotationApi = mapView.annotations
                                val pointAnnotationManager = annotationApi.createPointAnnotationManager()
                                
                                // This would require creating a bitmap for the user marker
                                // For simplicity, we're omitting the detailed implementation here
                            }
                            
                            // Add facility markers
                            val annotationApi = mapView.annotations
                            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
                            
                            facilities.forEach { facility ->
                                // Create a point for each facility
                                val point = Point.fromLngLat(facility.longitude, facility.latitude)
                                
                                // For simplicity we're omitting the bitmap creation
                                // You would need to create marker bitmaps based on availability
                                val pointAnnotationOptions = PointAnnotationOptions()
                                    .withPoint(point)
                                    // .withIconImage(markerBitmap)
                                
                                // Add click listener
                                val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
                                
                                // You would need to setup click listeners for markers
                                // This implementation is simplified
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mapView ->
                        // Update map when user location changes
                        userLocation?.let { location ->
                            val point = Point.fromLngLat(location.longitude, location.latitude)
                            mapView.getMapboxMap().setCamera(
                                CameraOptions.Builder()
                                    .center(point)
                                    .zoom(15.0)
                                    .build()
                            )
                        }
                    }
                )
            }
        }
    }
} 