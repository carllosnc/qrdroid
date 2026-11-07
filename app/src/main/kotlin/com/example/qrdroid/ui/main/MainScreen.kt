package com.example.qrdroid.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.qrdroid.data.repository.ScanRepository
import com.example.qrdroid.ui.create.CreateQrScreen
import com.example.qrdroid.ui.create.CreateQrViewModel
import com.example.qrdroid.ui.history.HistoryScreen
import com.example.qrdroid.ui.history.HistoryViewModel
import com.example.qrdroid.ui.scanner.ScannerScreen
import com.example.qrdroid.ui.scanner.ScannerViewModel

enum class MainTab {
    SCANNER,
    CREATE,
    HISTORY
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onItemClick: (NavKey) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val isDarkTheme = isSystemInDarkTheme()

    val repository = remember { ScanRepository(context) }
    val scannerViewModel: ScannerViewModel = viewModel { ScannerViewModel(repository) }
    val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(repository) }
    val createQrViewModel: CreateQrViewModel = viewModel { CreateQrViewModel() }

    var selectedTab by remember { mutableStateOf(MainTab.SCANNER) }

    // Pause camera hardware & torch when not on Scanner to prevent battery drain and overheating
    LaunchedEffect(selectedTab) {
        if (selectedTab != MainTab.SCANNER) {
            scannerViewModel.pauseScanner()
        } else {
            scannerViewModel.resumeScanner()
        }
    }

    // Status bar icon colors: ALWAYS white on Scanner; adapts to theme on other tabs
    DisposableEffect(selectedTab, isDarkTheme) {
        activity?.window?.let { window ->
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            if (selectedTab == MainTab.SCANNER) {
                insetsController.isAppearanceLightStatusBars = false
            } else {
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
            }
        }
        onDispose { }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Column {
                if (selectedTab != MainTab.SCANNER) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 0.8.dp
                    )
                }
                NavigationBar(
                    containerColor = if (selectedTab == MainTab.SCANNER) {
                        Color(0xF0121416)
                    } else {
                        MaterialTheme.colorScheme.background
                    },
                    tonalElevation = 0.dp
                ) {
                    val itemColors = if (selectedTab == MainTab.SCANNER) {
                        val accentBlue = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF87CFF9)
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = accentBlue,
                            selectedTextColor = accentBlue,
                            indicatorColor = accentBlue.copy(alpha = 0.28f),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        )
                    } else {
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    NavigationBarItem(
                        selected = selectedTab == MainTab.SCANNER,
                        onClick = { selectedTab = MainTab.SCANNER },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Aba Scanner"
                            )
                        },
                        label = {
                            Text(
                                text = "Scanner",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.CREATE,
                        onClick = { selectedTab = MainTab.CREATE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Aba Criar"
                            )
                        },
                        label = {
                            Text(
                                text = "Criar",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.HISTORY,
                        onClick = { selectedTab = MainTab.HISTORY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Aba Histórico"
                            )
                        },
                        label = {
                            Text(
                                text = "Histórico",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = itemColors
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // iOS-style slide & parallax page transition
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val isForward = targetState.ordinal > initialState.ordinal
                val animationEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
                val duration = 320

                if (isForward) {
                    // Slide in from right, exit with subtle parallax shift to the left
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = duration, easing = animationEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = duration)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 4 },
                            animationSpec = tween(durationMillis = duration, easing = animationEasing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration / 2))
                    )
                } else {
                    // Slide in from subtle parallax left, exit sliding to the right
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(durationMillis = duration, easing = animationEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = duration)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = duration, easing = animationEasing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration / 2))
                    )
                }
            },
            label = "IosTabTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                MainTab.SCANNER -> {
                    ScannerScreen(
                        viewModel = scannerViewModel,
                        isActive = (selectedTab == MainTab.SCANNER),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MainTab.CREATE -> {
                    CreateQrScreen(
                        viewModel = createQrViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MainTab.HISTORY -> {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
