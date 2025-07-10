package com.example.spendify_v1.ui.screen.gruppo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spendify_v1.ui.component.LineChart
import com.example.spendify_v1.ui.component.PieChart
import com.example.spendify_v1.ui.screen.home.LegendItem
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GruppoScreen(
    navController: NavHostController,
    gruppoId: String?,
    viewModel: GruppoViewModel = hiltViewModel()
) {
    val isLoading = viewModel.isLoading
    val nomeGruppo = viewModel.nomeGruppo

    val tabs = listOf("Per Tipologia", "Per Membro", "Andamento")
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nomeGruppo,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    when (page) {
                        0 -> PieChartTipologiaPage(viewModel)
                        1 -> PieChartMembriPage(viewModel)
                        2 -> LineChartPage(viewModel)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { if (gruppoId != null) navController.navigate("dettagliGruppo/$gruppoId") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dettagli Spese Gruppo")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { if (gruppoId != null) navController.navigate("informazioniGruppo/$gruppoId") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Informazioni Gruppo")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        viewModel.abbandonaGruppo()
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abbandona gruppo")
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PieChartTipologiaPage(viewModel: GruppoViewModel) {
    val chartData = viewModel.chartDataTipologie
    val totaleSpese = viewModel.totaleSpese
    val chartColors = viewModel.chartColors
    val legendData = viewModel.legendDataTipologie

    if (chartData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nessuna spesa registrata nel gruppo.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    // Questa column non ha bisogno di essere scrollabile (lo è già il genitore)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        PieChart(entries = chartData, totale = totaleSpese, colors = chartColors, showLegend = false)
        Spacer(modifier = Modifier.height(24.dp))
        CustomLegend(items = legendData)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PieChartMembriPage(viewModel: GruppoViewModel) {
    val chartData = viewModel.chartDataMembri
    val totaleSpese = viewModel.totaleSpese
    val chartColors = viewModel.chartColors
    val legendData = viewModel.legendDataMembri

    if (chartData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nessuna spesa da suddividere per membro.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        PieChart(entries = chartData, totale = totaleSpese, colors = chartColors, showLegend = false)
        Spacer(modifier = Modifier.height(24.dp))
        CustomLegend(items = legendData)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LineChartPage(viewModel: GruppoViewModel) {
    val lineData = viewModel.lineChartData
    val lineLabels = viewModel.lineChartXAxisLabels

    if (lineData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Dati insufficienti per l'andamento del gruppo.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        LineChart(entries = lineData, labels = lineLabels)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomLegend(items: List<LegendItem>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item -> LegendEntry(label = item.label, color = Color(item.color)) }
    }
}

@Composable
fun LegendEntry(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}