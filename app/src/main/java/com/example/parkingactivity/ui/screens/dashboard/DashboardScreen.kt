package com.example.parkingactivity.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
//import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkingactivity.data.FacilityType
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onFacilityClick: (String) -> Unit,
    onMapClick: () -> Unit,
    onActiveSessionClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val filteredFacilities by viewModel.filteredFacilities.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val selectedFacilityType by viewModel.selectedFacilityType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Park Smart") },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onProfileClick() }
                    )
                }
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = onMapClick) {
//                Icon(
//                    imageVector = Icons.Default.Map,
//                    contentDescription = "Map View"
//                )
//            }
//        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search parking facilities") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Facility type filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFacilityType == null,
                        onClick = { viewModel.setSelectedFacilityType(null) },
                        label = { Text("All") }
                    )
                }
                
                items(FacilityType.values()) { facilityType ->
                    FilterChip(
                        selected = selectedFacilityType == facilityType,
                        onClick = { viewModel.setSelectedFacilityType(facilityType) },
                        label = { Text(facilityType.name.lowercase().capitalize()) }
                    )
                }
            }
            
            // Active sessions section (if any)
            if (activeSessions.isNotEmpty()) {
                Text(
                    text = "Active Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(activeSessions) { session ->
                        ActiveSessionCard(
                            session = session,
                            onClick = { onActiveSessionClick(session.id) }
                        )
                    }
                }
            }
            
            // Available parking facilities
            Text(
                text = "Available Parking",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredFacilities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No parking facilities found")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredFacilities) { facility ->
                        FacilityCard(
                            facility = facility,
                            availableSpots = statuses[facility.id]?.availableSpots ?: 0,
                            onClick = { onFacilityClick(facility.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityCard(
    facility: ParkingFacility,
    availableSpots: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = facility.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = facility.address,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${facility.hourlyRate}/hour",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                val availabilityColor = when {
                    availableSpots > facility.totalSpots * 0.3 -> Color.Green
                    availableSpots > 0 -> Color.Yellow
                    else -> Color.Red
                }
                
                Text(
                    text = "$availableSpots spots available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = availabilityColor
                )
            }
        }
    }
}

@Composable
fun ActiveSessionCard(
    session: ParkingSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Active Parking",
                style = MaterialTheme.typography.titleSmall
            )
            
            Text(
                text = "Tap to view details",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 