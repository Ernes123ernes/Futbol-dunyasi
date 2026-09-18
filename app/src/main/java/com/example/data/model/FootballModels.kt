package com.example.data.model

data class GoalEvent(
    val minute: Int,
    val player: String,
    val team: String,
    val isPenalty: Boolean = false,
    val isOwnGoal: Boolean = false
)

data class CardEvent(
    val minute: Int,
    val player: String,
    val team: String,
    val isRed: Boolean = false
)

enum class TimelineEventType {
    GOAL, YELLOW_CARD, RED_CARD, VAR_DECISION, CHANCE, SUBSTITUTION
}

data class TimelineEvent(
    val minute: Int,
    val type: TimelineEventType,
    val title: String,
    val description: String,
    val team: String
)

data class MatchStatistics(
    val possessionHome: Int,
    val possessionAway: Int,
    val shotsHome: Int,
    val shotsAway: Int,
    val shotsOnTargetHome: Int,
    val shotsOnTargetAway: Int,
    val xGHome: Float,
    val xGAway: Float,
    val cornersHome: Int,
    val cornersAway: Int,
    val foulsHome: Int,
    val foulsAway: Int,
    val passAccuracyHome: Int,
    val passAccuracyAway: Int
)

data class LineupPlayer(
    val number: Int,
    val name: String,
    val position: String,
    val rating: Float
)

data class MatchSummary(
    val id: String,
    val league: String,
    val round: String,
    val matchDate: String,
    val statusText: String, // "MS" (Maç Sonu), "CANLI 74'", "20:00"
    val isLive: Boolean = false,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val homeColorHex: Long,
    val awayColorHex: Long,
    val venue: String,
    val referee: String,
    val summaryText: String,
    val videoHighlightDuration: String,
    val mvpPlayer: String,
    val goals: List<GoalEvent>,
    val cards: List<CardEvent>,
    val timeline: List<TimelineEvent>,
    val stats: MatchStatistics,
    val homeFormation: String = "4-2-3-1",
    val awayFormation: String = "4-3-3",
    val homeStarters: List<LineupPlayer> = emptyList(),
    val awayStarters: List<LineupPlayer> = emptyList()
)

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String, // "Süper Lig", "Transfer", "Avrupa", "Milli Takım"
    val author: String,
    val publishTime: String,
    val readTimeMinutes: Int,
    val bannerAccentColor: Long,
    val tags: List<String>,
    val isBreaking: Boolean = false
)

enum class QualificationType {
    CHAMPIONS_LEAGUE, EUROPA_LEAGUE, CONFERENCE_LEAGUE, NORMAL, RELEGATION
}

data class LeagueStanding(
    val rank: Int,
    val teamName: String,
    val shortName: String,
    val primaryColorHex: Long,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val points: Int,
    val formLast5: List<Char>, // e.g. ['G', 'G', 'B', 'M', 'G'] (G: Galibiyet, B: Beraberlik, M: Mağlubiyet)
    val qualification: QualificationType
)

data class PlayerStatLeader(
    val rank: Int,
    val name: String,
    val team: String,
    val value: Int,
    val secondaryStat: String, // e.g. "24 Maç", "5 Penaltı"
    val photoColorHex: Long
)

data class TeamAnalysis(
    val teamId: String,
    val teamName: String,
    val shortName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val manager: String,
    val formation: String,
    val tacticalStyle: String,
    val summary: String,
    val attackRating: Int,    // 0-100
    val defenseRating: Int,   // 0-100
    val possessionAvg: Int,   // %
    val xGPerMatch: Float,
    val pressingIntensity: String, // "Çok Yüksek", "Orta", "Kompakt"
    val strengths: List<String>,
    val weaknesses: List<String>,
    val keyPlayers: List<String>,
    val tacticalInsights: String,
    val recentFormTrend: String
)

data class UserComment(
    val id: Long = 0,
    val targetId: String, // matchId or newsId or "GENERAL"
    val authorName: String,
    val userTeam: String,
    val content: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class NotificationPreferences(
    val goalAlerts: Boolean = true,
    val redCardAlerts: Boolean = true,
    val matchStartReminderMinutes: Int = 15,
    val matchStartAlerts: Boolean = true,
    val fullTimeAlerts: Boolean = true,
    val highlightReadyAlerts: Boolean = true,
    val soundAndVibration: Boolean = true,
    val favoriteTeamOnly: Boolean = false,
    val favoriteTeam: String = "Galatasaray",
    val followedTeams: Set<String> = setOf("Galatasaray", "Fenerbahçe")
)

