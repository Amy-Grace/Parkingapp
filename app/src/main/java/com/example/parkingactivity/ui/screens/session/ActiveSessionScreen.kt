package com.example.parkingactivity.ui.screens.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.DirectionsCar
//import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkingactivity.ui.screens.session.ActiveSessionViewModel.CouponValidationState
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    sessionId: String,
    onPayClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val facility by viewModel.facility.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
//    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
//    val couponValidationState by viewModel.couponValidationState.collectAsState()
    
//    var showCouponDialog by remember { mutableStateOf(false) }
//    var couponCode by remember { mutableStateOf("") }
    var showEndSessionDialog by remember { mutableStateOf(false) }
    
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Parking") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (session == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Session not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Facility info
                facility?.let { f ->
                    Text(
                        text = f.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = f.address,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Session timer card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Parking Duration",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = duration,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        session?.spotNumber?.let { spot ->
                            Text(
                                text = "Spot: $spot",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Started: ${session?.entryTime?.toString() ?: ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Billing info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Billing Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rate",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            facility?.let {
                                Text(
                                    text = "₹${it.hourlyRate}/hour",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Applied coupon (if any)
//                        appliedCoupon?.let { coupon ->
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    text = "Coupon (${coupon.code})",
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
//                                Text(
//                                    text = "-${coupon.discountPercentage}%",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.primary
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//                        }
//
//                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Total amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Amount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${decimalFormat.format(totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add coupon button (if no coupon applied yet)
//                        if (appliedCoupon == null) {
//                            TextButton(
//                                onClick = { showCouponDialog = true },
//                                modifier = Modifier.align(Alignment.End)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.LocalOffer,
//                                    contentDescription = "Apply Coupon"
//                                )
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text("Apply Coupon")
//                            }
//                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showEndSessionDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("End Parking")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { onPayClick(sessionId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pay Now")
                    }
                }

                // Display coupon validation state
//                when (couponValidationState) {
//                    is CouponValidationState.Valid -> {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = "Coupon Applied",
//                                tint = Color.Green,
//                                modifier = Modifier.padding(end = 4.dp)
//                            )
//                            Text(
//                                text = "Coupon applied: ${(couponValidationState as CouponValidationState.Valid).coupon.code}",
//                                color = Color.Green,
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    }
//                    is CouponValidationState.Invalid -> {
//                        Text(
//                            text = (couponValidationState as CouponValidationState.Invalid).reason,
//                            color = Color.Red,
//                            style = MaterialTheme.typography.bodySmall,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 8.dp)
//                        )
//                    }
//                    is CouponValidationState.Loading -> {
//                        LinearProgressIndicator(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 8.dp)
//                        )
//                    }
//                    else -> {}
//                }
//            }
//        }
//    }

                // Coupon Dialog
//    if (showCouponDialog) {
//        AlertDialog(
//            onDismissRequest = { showCouponDialog = false },
//            title = { Text("Apply Coupon") },
//            text = {
//                Column {
//                    Text("Enter your coupon code to get a discount.")
//                    Spacer(modifier = Modifier.height(8.dp))
//                    OutlinedTextField(
//                        value = couponCode,
//                        onValueChange = { couponCode = it },
//                        label = { Text("Coupon Code") },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//            },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        showCouponDialog = false
//                        if (couponCode.isNotEmpty()) {
//                            viewModel.applyCoupon(couponCode)
//                        }
//                    }
//                ) {
//                    Text("Apply")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showCouponDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }

                // End Session Dialog
                if (showEndSessionDialog) {
                    AlertDialog(
                        onDismissRequest = { showEndSessionDialog = false },
                        title = { Text("End Parking Session") },
                        text = {
                            Text("Are you sure you want to end your parking session? You'll be directed to payment.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showEndSessionDialog = false
                                    viewModel.endSession {
                                        onPayClick(sessionId)
                                    }
                                }
                            ) {
                                Text("End Session")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndSessionDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }}}