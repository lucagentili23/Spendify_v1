package com.example.spendify_v1

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.spendify_v1.navigation.AppNavGraph
import com.example.spendify_v1.ui.theme.Spendify_v1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Il codice qui dentro funzionerà correttamente solo su dispositivi
    // con Android 8.0 (API 26) o versioni successive
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Spendify_v1Theme {
                // L'oggetto NavController viene creato solo una volta (quando l'ui
                // viene disegnata) successivamente non viene ricreata un'istanza
                // ma viene utilizzata quella già creata (remember)
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}