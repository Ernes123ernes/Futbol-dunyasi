package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FootballBottomNavigationBar
import com.example.ui.components.FootballTopBar
import com.example.ui.components.MatchDetailDialog
import com.example.ui.components.NotificationSettingsDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.FootballTab
import com.example.ui.viewmodel.FootballViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FootballViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemDark
            }

            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    viewModel.onNotificationPermissionGranted()
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MyApplicationTheme(themeMode = themeMode) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val targetComments by viewModel.targetComments.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.notificationSnackbarMessage) {
                    uiState.notificationSnackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearNotificationMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        FootballTopBar(
                            selectedLeague = uiState.selectedLeague,
                            onLeagueSelected = { viewModel.setSelectedLeague(it) },
                            activeRemindersCount = uiState.matchReminderIds.size,
                            isDarkMode = isDark,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onOpenNotificationSettings = { viewModel.openNotificationSettings() }
                        )
                    },
                    bottomBar = {
                        FootballBottomNavigationBar(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentTab) {
                            FootballTab.HIGHLIGHTS -> {
                                HighlightsScreen(
                                    matches = uiState.matches,
                                    currentFilter = uiState.matchFilter,
                                    remindedMatchIds = uiState.matchReminderIds,
                                    isDarkMode = isDark,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onToggleMatchReminder = { id, title ->
                                        viewModel.toggleMatchReminder(id, title)
                                    },
                                    onFilterSelected = { viewModel.setMatchFilter(it) },
                                    onMatchClick = { viewModel.selectMatch(it) },
                                    onWatchHighlight = {
                                        viewModel.selectMatch(it)
                                        viewModel.toggleHighlightPlayback()
                                    }
                                )
                            }
                            FootballTab.NEWS -> {
                                NewsScreen(
                                    newsList = uiState.newsList,
                                    selectedCategory = uiState.newsCategory,
                                    bookmarkedIds = uiState.bookmarkedNewsIds,
                                    matches = uiState.matches,
                                    isDarkMode = isDark,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onCategorySelected = { viewModel.setNewsCategory(it) },
                                    onNewsSelected = { viewModel.selectNews(it) },
                                    onToggleBookmark = { viewModel.toggleBookmarkNews(it) },
                                    onMatchClick = { viewModel.selectMatch(it) }
                                )
                            }
                            FootballTab.STANDINGS -> {
                                StandingsScreen(
                                    standings = uiState.standings,
                                    selectedLeague = uiState.selectedLeague,
                                    onNavigateToCompare = { teamName ->
                                        val otherTeam = if (teamName == "Galatasaray") "Fenerbahçe" else "Galatasaray"
                                        viewModel.setCompareTeams(teamName, otherTeam)
                                        viewModel.setTab(FootballTab.ANALYSIS)
                                    }
                                )
                            }
                            FootballTab.STATS -> {
                                StatisticsScreen(
                                    topScorers = uiState.topScorers,
                                    topAssists = uiState.topAssists
                                )
                            }
                            FootballTab.ANALYSIS -> {
                                TeamAnalysisScreen(
                                    analyses = uiState.teamAnalyses,
                                    selectedAnalysis = uiState.selectedTeamAnalysis,
                                    compareTeamAId = uiState.compareTeamAId,
                                    compareTeamBId = uiState.compareTeamBId,
                                    onSelectTeam = { viewModel.selectTeamAnalysis(it) },
                                    onSetCompareTeams = { a, b -> viewModel.setCompareTeams(a, b) },
                                    standings = uiState.standings
                                )
                            }
                        }
                    }

                    // Notification & Reminder Settings Dialog
                    if (uiState.isNotificationSettingsOpen) {
                        NotificationSettingsDialog(
                            preferences = uiState.notificationPreferences,
                            matches = uiState.matches,
                            remindedMatchIds = uiState.matchReminderIds,
                            themeMode = themeMode,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            onUpdatePreferences = { viewModel.updateNotificationPreferences(it) },
                            onToggleMatchReminder = { id, title ->
                                viewModel.toggleMatchReminder(id, title)
                            },
                            onTriggerTestGoal = { viewModel.triggerTestGoalNotification() },
                            onTriggerTestMatchStart = { viewModel.triggerTestMatchStartNotification() },
                            onToggleFollowedTeam = { viewModel.toggleFollowedTeam(it) },
                            onDismiss = { viewModel.closeNotificationSettings() }
                        )
                    }

                    // Match Detail & Highlights Dialog
                    uiState.selectedMatch?.let { match ->
                        MatchDetailDialog(
                            match = match,
                            comments = targetComments,
                            isVideoPlaying = uiState.isHighlightVideoPlaying,
                            currentMinute = uiState.currentHighlightMinute,
                            isReminded = uiState.matchReminderIds.contains(match.id),
                            isDarkMode = isDark,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onToggleReminder = {
                                viewModel.toggleMatchReminder(match.id, "${match.homeTeam} vs ${match.awayTeam}")
                            },
                            onTogglePlay = { viewModel.toggleHighlightPlayback() },
                            onMinuteSelected = { viewModel.jumpToHighlightMinute(it) },
                            onAddComment = { author, team, content ->
                                viewModel.addComment(author, team, content)
                            },
                            onToggleLike = { id, liked ->
                                viewModel.toggleLike(id, liked)
                            },
                            onDismiss = { viewModel.selectMatch(null) }
                        )
                    }

                    // News Detail & Reading Dialog
                    uiState.selectedNews?.let { news ->
                        NewsDetailDialog(
                            news = news,
                            comments = targetComments,
                            isBookmarked = uiState.bookmarkedNewsIds.contains(news.id),
                            onToggleBookmark = { viewModel.toggleBookmarkNews(news.id) },
                            onAddComment = { author, team, content ->
                                viewModel.addComment(author, team, content)
                            },
                            onToggleLike = { id, liked ->
                                viewModel.toggleLike(id, liked)
                            },
                            onDismiss = { viewModel.selectNews(null) }
                        )
                    }
                }
            }
        }
    }
}

// Greeting helper retained for test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
