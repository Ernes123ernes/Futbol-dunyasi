package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FootballRepository
import com.example.service.FootballNotificationService
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FootballTab(val title: String) {
    HIGHLIGHTS("Özetler"),
    NEWS("Haberler"),
    STANDINGS("Puan"),
    STATS("İstatistik"),
    ANALYSIS("Analiz")
}

enum class MatchFilter(val label: String) {
    ALL("Tüm Maçlar"),
    HIGHLIGHTS_ONLY("Özetler"),
    LIVE("Canlı"),
    FIXTURES("Fikstür")
}

data class FootballUiState(
    val currentTab: FootballTab = FootballTab.HIGHLIGHTS,
    val selectedLeague: String = "Trendyol Süper Lig",
    val matchFilter: MatchFilter = MatchFilter.ALL,
    val newsCategory: String = "Tümü",
    val matches: List<MatchSummary> = emptyList(),
    val newsList: List<NewsArticle> = emptyList(),
    val standings: List<LeagueStanding> = emptyList(),
    val topScorers: List<PlayerStatLeader> = emptyList(),
    val topAssists: List<PlayerStatLeader> = emptyList(),
    val teamAnalyses: List<TeamAnalysis> = emptyList(),
    val selectedMatch: MatchSummary? = null,
    val selectedNews: NewsArticle? = null,
    val selectedTeamAnalysis: TeamAnalysis? = null,
    val compareTeamAId: String = "gs",
    val compareTeamBId: String = "fb",
    val bookmarkedNewsIds: Set<String> = emptySet(),
    val isHighlightVideoPlaying: Boolean = false,
    val currentHighlightMinute: Int = 0,
    val searchQuery: String = "",
    val activeCommentTargetId: String = "match_1",
    val isNotificationSettingsOpen: Boolean = false,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val matchReminderIds: Set<String> = setOf("match_2"),
    val notificationSnackbarMessage: String? = null
)

class FootballViewModel(application: Application) : AndroidViewModel(application) {

    private val themeManager = ThemeManager.getInstance(application)
    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode

    private val notificationService = FootballNotificationService.getInstance(application)
    private val matchScoreTracker = mutableMapOf<String, Pair<Int, Int>>()
    private val notifiedMatchStartIds = mutableSetOf<String>()

    private val repository: FootballRepository
    private val _uiState = MutableStateFlow(FootballUiState())
    val uiState: StateFlow<FootballUiState> = _uiState.asStateFlow()

    // Comments reactive flow for the currently active target
    private val _targetComments = MutableStateFlow<List<UserComment>>(emptyList())
    val targetComments: StateFlow<List<UserComment>> = _targetComments.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FootballRepository(db.commentDao())

        // Load repository static and dynamic data
        val matches = repository.getMatches()
        val news = repository.getNews()
        val standings = repository.getStandings()
        val scorers = repository.getTopScorers()
        val assists = repository.getTopAssists()
        val analyses = repository.getTeamAnalyses()

        matches.forEach { match ->
            matchScoreTracker[match.id] = Pair(match.homeScore, match.awayScore)
            if (match.isLive) {
                notifiedMatchStartIds.add(match.id)
            }
        }

        _uiState.update {
            it.copy(
                matches = matches,
                newsList = news,
                standings = standings,
                topScorers = scorers,
                topAssists = assists,
                teamAnalyses = analyses,
                selectedTeamAnalysis = analyses.firstOrNull()
            )
        }

        viewModelScope.launch {
            repository.seedInitialCommentsIfEmpty()
            observeComments("match_1")
        }

        startRealtimeLiveMatchTicker()
    }

    private fun startRealtimeLiveMatchTicker() {
        viewModelScope.launch {
            var tickCount = 0
            while (true) {
                delay(3000) // Ticks every 3 seconds for visible live updates
                tickCount++

                _uiState.update { state ->
                    val prefs = state.notificationPreferences
                    val updatedMatches = state.matches.map { match ->
                        if (match.isLive) {
                            // Check if match start notification should be triggered
                            if (!notifiedMatchStartIds.contains(match.id)) {
                                notifiedMatchStartIds.add(match.id)
                                notificationService.notifyMatchStart(match, prefs)
                            }

                            val currentMin = match.statusText
                                .filter { it.isDigit() }
                                .toIntOrNull() ?: 75
                            val nextMin = if (currentMin >= 90) 90 else currentMin + 1
                            val newStatus = if (nextMin >= 90) "90+${(tickCount % 5) + 1}'" else "CANLI $nextMin'"

                            // Periodic simulated goal event in a live match every ~15 ticks for demonstration
                            var updatedHomeScore = match.homeScore
                            var updatedAwayScore = match.awayScore
                            if (tickCount % 15 == 0 && match.id == "match_1") {
                                updatedHomeScore += 1
                                notificationService.notifyGoal(
                                    match = match.copy(homeScore = updatedHomeScore),
                                    scoringTeam = match.homeTeam,
                                    scorerName = "Victor Osimhen",
                                    minute = nextMin,
                                    preferences = prefs
                                )
                            }

                            match.copy(
                                statusText = newStatus,
                                homeScore = updatedHomeScore,
                                awayScore = updatedAwayScore
                            )
                        } else {
                            match
                        }
                    }

                    val updatedSelected = if (state.selectedMatch?.isLive == true) {
                        updatedMatches.find { it.id == state.selectedMatch.id } ?: state.selectedMatch
                    } else {
                        state.selectedMatch
                    }

                    state.copy(
                        matches = updatedMatches,
                        selectedMatch = updatedSelected
                    )
                }
            }
        }
    }

    fun setTab(tab: FootballTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setMatchFilter(filter: MatchFilter) {
        _uiState.update { it.copy(matchFilter = filter) }
    }

    fun setNewsCategory(cat: String) {
        _uiState.update { it.copy(newsCategory = cat) }
    }

    fun setSelectedLeague(league: String) {
        _uiState.update { it.copy(selectedLeague = league) }
    }

    fun selectMatch(match: MatchSummary?) {
        _uiState.update {
            it.copy(
                selectedMatch = match,
                isHighlightVideoPlaying = false,
                currentHighlightMinute = 0
            )
        }
        if (match != null) {
            observeComments(match.id)
        }
    }

    fun selectNews(news: NewsArticle?) {
        _uiState.update { it.copy(selectedNews = news) }
        if (news != null) {
            observeComments(news.id)
        }
    }

    fun selectTeamAnalysis(analysis: TeamAnalysis) {
        _uiState.update { it.copy(selectedTeamAnalysis = analysis) }
    }

    fun setCompareTeams(teamA: String, teamB: String) {
        _uiState.update { it.copy(compareTeamAId = teamA, compareTeamBId = teamB) }
    }

    fun toggleBookmarkNews(newsId: String) {
        _uiState.update { state ->
            val set = state.bookmarkedNewsIds.toMutableSet()
            if (set.contains(newsId)) set.remove(newsId) else set.add(newsId)
            state.copy(bookmarkedNewsIds = set)
        }
    }

    fun toggleHighlightPlayback() {
        val playing = !_uiState.value.isHighlightVideoPlaying
        _uiState.update { it.copy(isHighlightVideoPlaying = playing) }
        if (playing) {
            simulateVideoProgress()
        }
    }

    private fun simulateVideoProgress() {
        viewModelScope.launch {
            while (_uiState.value.isHighlightVideoPlaying) {
                delay(1200)
                _uiState.update { state ->
                    val nextMinute = if (state.currentHighlightMinute >= 90) 0 else state.currentHighlightMinute + 15
                    state.copy(currentHighlightMinute = nextMinute)
                }
            }
        }
    }

    fun jumpToHighlightMinute(minute: Int) {
        _uiState.update { it.copy(currentHighlightMinute = minute) }
    }

    fun observeComments(targetId: String) {
        _uiState.update { it.copy(activeCommentTargetId = targetId) }
        viewModelScope.launch {
            repository.getCommentsForTarget(targetId).collect { comments ->
                _targetComments.value = comments
            }
        }
    }

    fun addComment(author: String, team: String, content: String) {
        if (content.isBlank()) return
        val targetId = _uiState.value.activeCommentTargetId
        val authorName = if (author.isBlank()) "Futbol Sever" else author.trim()
        val favoriteTeam = if (team.isBlank()) "Genel" else team.trim()

        viewModelScope.launch {
            repository.addComment(
                targetId = targetId,
                authorName = authorName,
                userTeam = favoriteTeam,
                content = content.trim()
            )
        }
    }

    fun toggleLike(commentId: Long, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLike(commentId, currentLiked)
        }
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun openNotificationSettings() {
        _uiState.update { it.copy(isNotificationSettingsOpen = true) }
    }

    fun closeNotificationSettings() {
        _uiState.update { it.copy(isNotificationSettingsOpen = false) }
    }

    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        _uiState.update {
            it.copy(
                notificationPreferences = preferences,
                notificationSnackbarMessage = "Bildirim tercihleri güncellendi"
            )
        }
    }

    fun toggleMatchReminder(matchId: String, matchTitle: String) {
        _uiState.update { state ->
            val isCurrentlyReminded = state.matchReminderIds.contains(matchId)
            val updatedReminders = if (isCurrentlyReminded) {
                state.matchReminderIds - matchId
            } else {
                state.matchReminderIds + matchId
            }
            val msg = if (isCurrentlyReminded) {
                "$matchTitle hatırlatıcısı kaldırıldı."
            } else {
                "🔔 $matchTitle için maçtan 15 dk önce bildirim kuruldu!"
            }
            state.copy(
                matchReminderIds = updatedReminders,
                notificationSnackbarMessage = msg
            )
        }
    }

    fun clearNotificationMessage() {
        _uiState.update { it.copy(notificationSnackbarMessage = null) }
    }

    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun toggleTheme() {
        themeManager.toggleDarkLight()
        val currentMode = themeManager.themeMode.value
        val msg = if (currentMode == ThemeMode.DARK) "Karanlık Mod (Stadyum Gece) etkinleştirildi" else "Aydınlık Mod etkinleştirildi"
        _uiState.update { it.copy(notificationSnackbarMessage = msg) }
    }

    fun triggerTestGoalNotification(teamName: String? = null) {
        val prefs = _uiState.value.notificationPreferences
        val targetTeam = teamName ?: prefs.favoriteTeam.takeIf { it != "Tüm Takımlar" }
            ?: prefs.followedTeams.firstOrNull() ?: "Galatasaray"

        val liveOrFirstMatch = _uiState.value.matches.find {
            it.homeTeam.contains(targetTeam, ignoreCase = true) || it.awayTeam.contains(targetTeam, ignoreCase = true)
        } ?: _uiState.value.matches.first()

        val isHome = liveOrFirstMatch.homeTeam.contains(targetTeam, ignoreCase = true)
        val scorer = if (isHome) {
            if (targetTeam.contains("Galatasaray")) "Victor Osimhen"
            else if (targetTeam.contains("Fenerbahçe")) "Edin Džeko"
            else "Ciro Immobile"
        } else {
            "Rafa Silva"
        }
        val min = 64
        val simulatedMatch = liveOrFirstMatch.copy(
            homeScore = if (isHome) liveOrFirstMatch.homeScore + 1 else liveOrFirstMatch.homeScore,
            awayScore = if (!isHome) liveOrFirstMatch.awayScore + 1 else liveOrFirstMatch.awayScore,
            isLive = true,
            statusText = "CANLI $min'"
        )

        val delivered = notificationService.notifyGoal(
            match = simulatedMatch,
            scoringTeam = targetTeam,
            scorerName = scorer,
            minute = min,
            preferences = prefs
        )

        val msg = if (delivered) {
            "⚽ Sistem Bildirimi Gönderildi: GOOOLLL! $targetTeam ($scorer $min')"
        } else {
            "⚽ Gol Bildirimi simüle edildi: $targetTeam ($scorer $min')."
        }
        _uiState.update { it.copy(notificationSnackbarMessage = msg) }
    }

    fun triggerTestMatchStartNotification(teamName: String? = null) {
        val prefs = _uiState.value.notificationPreferences
        val targetTeam = teamName ?: prefs.favoriteTeam.takeIf { it != "Tüm Takımlar" }
            ?: prefs.followedTeams.firstOrNull() ?: "Galatasaray"

        val match = _uiState.value.matches.find {
            it.homeTeam.contains(targetTeam, ignoreCase = true) || it.awayTeam.contains(targetTeam, ignoreCase = true)
        } ?: _uiState.value.matches.first()

        val delivered = notificationService.notifyMatchStart(
            match = match,
            preferences = prefs
        )

        val msg = if (delivered) {
            "⏱️ Sistem Bildirimi Gönderildi: Maç Başladı! (${match.homeTeam} vs ${match.awayTeam})"
        } else {
            "⏱️ Maç Başlangıç Bildirimi simüle edildi (${match.homeTeam} vs ${match.awayTeam})."
        }
        _uiState.update { it.copy(notificationSnackbarMessage = msg) }
    }

    fun toggleFollowedTeam(teamName: String) {
        val currentFollowed = _uiState.value.notificationPreferences.followedTeams.toMutableSet()
        val isAdding = !currentFollowed.contains(teamName)
        if (isAdding) {
            currentFollowed.add(teamName)
        } else {
            currentFollowed.remove(teamName)
        }
        val updatedPrefs = _uiState.value.notificationPreferences.copy(followedTeams = currentFollowed)
        val msg = if (isAdding) "$teamName takip edilen takımlara eklendi" else "$teamName takipten çıkarıldı"
        _uiState.update {
            it.copy(
                notificationPreferences = updatedPrefs,
                notificationSnackbarMessage = msg
            )
        }
    }

    fun onNotificationPermissionGranted() {
        _uiState.update {
            it.copy(notificationSnackbarMessage = "🔔 Bildirim izni etkinleştirildi! Takip ettiğiniz takımların gol ve maç başlangıç bildirimleri anında gelecektir.")
        }
    }
}
