package com.example.spendify_v1.ui.screen.dettagli

import SpesaItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.spendify_v1.data.model.Spesa
import kotlinx.coroutines.launch

// Annotazione necessaria per usare il pager
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DettagliScreen(
    viewModel: DettagliViewModel = hiltViewModel(),
    navController: NavHostController
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
                    text = "Dettagli Spese",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )

                // TabRow per la navigazione tra le due sezioni
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

                // Pager che contiene le due liste
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Occupa lo spazio rimanente
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> SpeseList(
                            spese = speseList,
                            emptyListMessage = "Nessuna spesa registrata.",
                            showMoreButton = canShowMoreRegistrate,
                            onShowMoreClicked = { viewModel.mostraAltreSpeseRegistrate() },
                            onSpesaClick = navigateToDetail
                        )
                        1 -> SpeseList(
                            spese = speseInEntrataList,
                            emptyListMessage = "Nessuna spesa futura programmata.",
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

@Composable
fun SpeseList(
    spese: List<Spesa>,
    emptyListMessage: String,
    showMoreButton: Boolean,
    onShowMoreClicked: () -> Unit,
    onSpesaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (spese.isEmpty()) {
            Text(
                text = emptyListMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(spese, key = { it.id }) { spesa ->
                    SpesaItem(
                        spesa = spesa,
                        onItemClick = { onSpesaClick(spesa.id) }
                    )
                }

                if (showMoreButton) {
                    item {
                        Button(
                            onClick = onShowMoreClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Mostra altro")
                        }
                    }
                }
            }
        }
    }
}