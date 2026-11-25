package com.example.lab14widgets

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.walletDataStore by preferencesDataStore(name = "wallet_prefs")

data class WalletUiState(
    val balance: Double = 485.50,
    val lastMovement: String = "Último: Pago QR · -S/ 18.00",
    val isHidden: Boolean = false,
    val history: List<String> = listOf(
        "Pago QR · -S/ 18.00",
        "Ingreso Work · +S/ 120.00",
        "Recarga app · +S/ 50.00"
    )
)

object WalletStateManager {
    private val BALANCE_KEY = doublePreferencesKey("balance")
    private val LAST_MOVEMENT_KEY = stringPreferencesKey("last_movement")
    private val IS_HIDDEN_KEY = booleanPreferencesKey("is_hidden")
    private val HISTORY_KEY = stringPreferencesKey("history")

    fun stateFlow(context: Context): Flow<WalletUiState> {
        return context.walletDataStore.data.map { prefs ->
            val rawHistory = prefs[HISTORY_KEY]
            val parsedHistory = if (rawHistory.isNullOrBlank()) {
                WalletUiState().history
            } else {
                rawHistory.split("||").filter { it.isNotBlank() }
            }

            WalletUiState(
                balance = prefs[BALANCE_KEY] ?: 485.50,
                lastMovement = prefs[LAST_MOVEMENT_KEY] ?: "Último: Pago QR · -S/ 18.00",
                isHidden = prefs[IS_HIDDEN_KEY] ?: false,
                history = parsedHistory
            )
        }
    }

    suspend fun toggleHidden(context: Context) {
        context.walletDataStore.edit { prefs ->
            val current = prefs[IS_HIDDEN_KEY] ?: false
            prefs[IS_HIDDEN_KEY] = !current
        }
        WalletWidgetContent().updateAll(context)
    }

    suspend fun applyMovement(context: Context, title: String, delta: Double) {
        context.walletDataStore.edit { prefs ->
            val currentBalance = prefs[BALANCE_KEY] ?: 485.50
            val newBalance = (currentBalance + delta).coerceAtLeast(0.0)
            val sign = if (delta >= 0) "+" else "-"
            val formattedAmount = String.format(Locale.US, "%.2f", kotlin.math.abs(delta))
            val movementItem = "$title · ${sign}S/ $formattedAmount"

            val currentHistory = prefs[HISTORY_KEY]
                ?.split("||")
                ?.filter { it.isNotBlank() }
                ?: WalletUiState().history

            val updatedHistory = (listOf(movementItem) + currentHistory).take(5)

            prefs[BALANCE_KEY] = newBalance
            prefs[LAST_MOVEMENT_KEY] = "Último: $movementItem"
            prefs[HISTORY_KEY] = updatedHistory.joinToString("||")
        }
        WalletWidgetContent().updateAll(context)
    }
}