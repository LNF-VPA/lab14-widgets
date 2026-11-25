package com.example.lab14widgets

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lab14widgets.ui.theme.Lab14WidgetsTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab14WidgetsTheme {
                WalletTabsScreen(selectedTab = 0)
            }
        }
    }
}

@Composable
fun WalletTabsScreen(selectedTab: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by WalletStateManager.stateFlow(context).collectAsState(initial = WalletUiState())

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        if (selectedTab != 0) {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        }
                    },
                    text = { Text("Hello Android") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        if (selectedTab != 1) {
                            context.startActivity(Intent(context, WorkActivity::class.java))
                        }
                    },
                    text = { Text("Work") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (selectedTab == 0) "Hello Android · Billetera" else "Work · Movimientos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val formattedBalance = String.format(Locale.US, "%.2f", state.balance)
                        Text(
                            text = if (state.isHidden) "Saldo: S/ ••••••" else "Saldo: S/ $formattedBalance",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.lastMovement,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (selectedTab == 0) {
                    Text(
                        text = "Acciones rápidas (actualizan el widget):",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    WalletStateManager.applyMovement(context, "Pago Hello QR", -15.0)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pagar -S/ 15")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    WalletStateManager.applyMovement(context, "Recarga rápida", 30.0)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Recargar +S/ 30")
                        }
                    }
                } else {
                    Text(
                        text = "Actividad de trabajo y recientes:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    WalletStateManager.applyMovement(context, "Pago Work", 85.0)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cobro Work +S/ 85")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    WalletStateManager.applyMovement(context, "Gasto oficina", -20.0)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Gasto -S/ 20")
                        }
                    }

                    state.history.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}