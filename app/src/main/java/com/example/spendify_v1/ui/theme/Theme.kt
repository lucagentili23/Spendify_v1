package com.example.spendify_v1.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E5EB9),
    secondary = Color(0xFF4F73B4),
    tertiary = Color(0xFF86A1DA)
)

@Composable
fun Spendify_v1Theme(
    content: @Composable () -> Unit
) {
    // Controlla se usare i colori dinamici chiari o lo schema chiaro di default.
    val colorScheme = LightColorScheme

    // Ottiene la View Android sottostante che ospita i Composable.
    val view = LocalView.current
    // Esegue il codice solo quando l'app è in esecuzione
    if (!view.isInEditMode) {
        SideEffect {
            // Ottiene l'oggetto "Window" dell'Activity corrente.
            val window = (view.context as Activity).window
            // Imposta il colore di sfondo della barra di stato a trasparente.
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            // Imposta le icone della status bar nere
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}