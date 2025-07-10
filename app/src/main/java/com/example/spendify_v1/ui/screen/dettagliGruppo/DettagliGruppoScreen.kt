package com.example.spendify_v1.ui.screen.dettagliGruppo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spendify_v1.ui.screen.dettagli.SpeseList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DettagliGruppoScreen(
    navController: NavHostController,
    viewModel: DettagliGruppoViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val speseList by viewModel.speseEntrate.collectAsState()
    val speseInEntrataList by viewModel.speseInEntrata.collectAsState()
    val canShowMoreRegistrate by viewModel.canShowMoreRegistrate.collectAsState()
    val canShowMoreFuture by viewModel.canShowMoreFuture.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("Spese registrate", "Spese in arrivo")

    val navigateToDetail: (String) -> Unit = { spesaId ->
        navController.navigate("dettaglioSpesa/$spesaId")
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Dettagli Spese del Gruppo",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )

                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = title) }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> SpeseList(
                            spese = speseList,
                            emptyListMessage = "Nessuna spesa registrata per il gruppo.",
                            showMoreButton = canShowMoreRegistrate,
                            onShowMoreClicked = { viewModel.mostraAltreSpeseRegistrate() },
                            onSpesaClick = navigateToDetail
                        )
                        1 -> SpeseList(
                            spese = speseInEntrataList,
                            emptyListMessage = "Nessuna spesa futura programmata per il gruppo.",
                            showMoreButton = canShowMoreFuture,
                            onShowMoreClicked = { viewModel.mostraAltreSpeseFuture() },
                            onSpesaClick = navigateToDetail
                        )
                    }
                }
            }
        }
    }
}