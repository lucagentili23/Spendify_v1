package com.example.spendify_v1.ui.component

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    entries: List<PieEntry>,
    totale: Double,
    colors: List<Int>,
    showLegend: Boolean = false // Parametro per controllare la legenda nativa
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                // Imposta l'uso delle percentuali una sola volta
                setUsePercentValues(true)
            }
        },
        update = { pieChart ->
            val dataSet = PieDataSet(entries, "Spese")
            dataSet.colors = colors
            dataSet.sliceSpace = 3f
            dataSet.setDrawIcons(false)

            val data = PieData(dataSet)
            // Formatta i valori come percentuali
            data.setValueFormatter(PercentFormatter(pieChart))
            data.setValueTextSize(14f)
            data.setValueTextColor(Color.BLACK)

            pieChart.data = data
            pieChart.description.isEnabled = false
            pieChart.legend.isEnabled = showLegend

            pieChart.isDrawHoleEnabled = true
            pieChart.holeRadius = 60f
            pieChart.setDrawSlicesUnderHole(true)

            pieChart.centerText = "Totale speso:\n${String.format("%.2f €", totale)}"
            pieChart.setCenterTextSize(20f)
            pieChart.setDrawEntryLabels(false)

            // Aggiorna e anima
            pieChart.invalidate()
            pieChart.animateY(1200)
        }
    )
}