package com.example.spendify_v1.ui.component

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.spendify_v1.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    entries: List<Entry>,
    labels: List<String>
) {
    val context = LocalContext.current
    val gradientFill = ContextCompat.getDrawable(context, R.drawable.chart_gradient_fill)

    AndroidView(
        modifier = modifier.fillMaxWidth().height(300.dp),
        factory = { ctx -> LineChart(ctx) },
        update = { lineChart ->
            val dataSet = LineDataSet(entries, "Spese Giornaliere").apply {
                // Linea curva e morbida
                mode = LineDataSet.Mode.LINEAR

                // Colori e spessore della linea
                color = Color.parseColor("#007AFF") // Un blu più moderno
                lineWidth = 2.5f

                // Area sfumata sotto la linea
                setDrawFilled(true)
                fillDrawable = gradientFill

                // Punti più raffinati
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(Color.parseColor("#007AFF"))
                // Cerchio interno bianco
                setDrawCircleHole(true)
                circleHoleColor = Color.WHITE
                circleHoleRadius = 2f

                // Nascondo i valori numerici sopra i punti (uso il marker)
                setDrawValues(false)

                // Rimuovo l'evidenziazione di default per usare solo il marker
                highLightColor = Color.TRANSPARENT
            }

            lineChart.apply {
                data = LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false

                setDrawBorders(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(labels)
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    textColor = Color.GRAY
                    textSize = 12f
                    // Inizio l'asse un po' prima per estetica
                    axisMinimum = -0.1f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.parseColor("#E0E0E0")
                    setDrawAxisLine(false)
                    textColor = Color.GRAY
                    textSize = 12f
                    axisMinimum = 0f
                }

                axisRight.isEnabled = false

                setTouchEnabled(true)
                setPinchZoom(true) // Permette di zoomare con due dita
                // Imposto il marker personalizzato
                marker = MyMarkerView(context, R.layout.marker_view)

                invalidate()
                animateX(1000)
            }
        }
    )
}


class MyMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    // Viene chiamato ogni volta che il marker viene disegnato
    override fun refreshContent(e: Entry, highlight: Highlight) {
        // Formatto il valore da mostrare
        tvContent.text = String.format("%.2f €", e.y)
        super.refreshContent(e, highlight)
    }

    // Imposta la posizione del marker rispetto al punto toccato
    override fun getOffset(): MPPointF {
        // Per centrarlo sopra il punto
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
}