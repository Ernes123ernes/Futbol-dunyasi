package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*

enum class MatchDetailTab(val label: String) {
    HIGHLIGHTS("Özet & Anlar"),
    STATS("İstatistik"),
    LINEUPS("Kadrolar"),
    COMMENTS("Yorumlar")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailDialog(
    match: MatchSummary,
    comments: List<UserComment>,
    isVideoPlaying: Boolean,
    currentMinute: Int,
    isReminded: Boolean = false,
    isDarkMode: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    onToggleReminder: (() -> Unit)? = null,
    onTogglePlay: () -> Unit,
    onMinuteSelected: (Int) -> Unit,
    onAddComment: (author: String, team: String, content: String) -> Unit,
    onToggleLike: (commentId: Long, currentLiked: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MatchDetailTab.HIGHLIGHTS) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_match_detail_btn")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                    Text(
                        text = "${match.homeTeam} vs ${match.awayTeam}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onToggleReminder != null) {
                            IconButton(
                                onClick = onToggleReminder,
                                modifier = Modifier.testTag("match_detail_reminder_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isReminded) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                    contentDescription = "Hatırlatıcı",
                                    tint = if (isReminded) PitchGreenLight else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { /* Share */ }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Paylaş")
                        }
                    }
                }

                // Match Hero Scoreboard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${match.league} • ${match.round}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Home Team
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(match.homeColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.homeTeam.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = match.homeTeam,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Score & Status
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${match.homeScore} - ${match.awayScore}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = PitchGreenLight
                                )
                                Text(
                                    text = match.statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Away Team
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(match.awayColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.awayTeam.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = match.awayTeam,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📍 ${match.venue} • 👤 Hakem: ${match.referee}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sub-tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MatchDetailTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Content Area
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        MatchDetailTab.HIGHLIGHTS -> HighlightsTabContent(
                            match = match,
                            isVideoPlaying = isVideoPlaying,
                            currentMinute = currentMinute,
                            isDarkMode = isDarkMode,
                            onToggleTheme = onToggleTheme,
                            onTogglePlay = onTogglePlay,
                            onMinuteSelected = onMinuteSelected
                        )
                        MatchDetailTab.STATS -> StatsTabContent(match = match)
                        MatchDetailTab.LINEUPS -> LineupsTabContent(match = match)
                        MatchDetailTab.COMMENTS -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                item {
                                    CommentSection(
                                        comments = comments,
                                        onAddComment = onAddComment,
                                        onToggleLike = onToggleLike
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightsTabContent(
    match: MatchSummary,
    isVideoPlaying: Boolean,
    currentMinute: Int,
    isDarkMode: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    onTogglePlay: () -> Unit,
    onMinuteSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Video Highlight Simulator Player Box
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.PlayCircleFilled, contentDescription = null, tint = PitchGreenLight)
                            Text("Maç Geniş Özeti", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (onToggleTheme != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .clickable { onToggleTheme() }
                                        .testTag("match_detail_video_theme_toggle_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "Tema Değiştir",
                                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isDarkMode) "Gece Modu" else "Aydınlık",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(match.videoHighlightDuration, style = MaterialTheme.typography.labelMedium, color = GoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Video Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF030712)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(PitchGreenLight.copy(alpha = 0.85f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Oynat/Durdur",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isVideoPlaying) "Oynatılıyor: Dakika $currentMinute'" else "Özeti İzlemek İçin Dokunun",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timeline quick moment scrubbers
                    Text(
                        text = "Kritik Anlara Git:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        match.timeline.take(4).forEach { event ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentMinute == event.minute) PitchGreenLight else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .clickable { onMinuteSelected(event.minute) }
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${event.minute}'",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentMinute == event.minute) Color.White else PitchGreenLight
                                    )
                                    Text(
                                        text = if (event.type == TimelineEventType.GOAL) "⚽ Gol" else "⚠️ Pozisyon",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (currentMinute == event.minute) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Match Summary Text
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Maç Değerlendirmesi & MVP",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ Maçın Oyuncusu: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(match.mvpPlayer, color = GoldAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Minute by Minute Timeline
        item {
            Text(
                text = "Maçın Önemli Olayları",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(match.timeline) { event ->
            TimelineEventCard(event = event)
        }
    }
}

@Composable
fun TimelineEventCard(event: TimelineEvent) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when (event.type) {
                    TimelineEventType.GOAL -> PitchGreenLight
                    TimelineEventType.RED_CARD -> RedCardColor
                    TimelineEventType.YELLOW_CARD -> YellowCardColor
                    TimelineEventType.VAR_DECISION -> Color(0xFF8B5CF6)
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${event.minute}'",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = event.team,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsTabContent(match: MatchSummary) {
    val stats = match.stats
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Takım İstatistik Karşılaştırması",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            StatComparisonRow(
                title = "Topa Sahip Olma (%)",
                homeVal = "${stats.possessionHome}%",
                awayVal = "${stats.possessionAway}%",
                homePercent = stats.possessionHome / 100f
            )
        }
        item {
            val totalShots = (stats.shotsHome + stats.shotsAway).coerceAtLeast(1)
            StatComparisonRow(
                title = "Toplam Şut",
                homeVal = "${stats.shotsHome}",
                awayVal = "${stats.shotsAway}",
                homePercent = stats.shotsHome.toFloat() / totalShots
            )
        }
        item {
            val totalTarget = (stats.shotsOnTargetHome + stats.shotsOnTargetAway).coerceAtLeast(1)
            StatComparisonRow(
                title = "İsabetli Şut",
                homeVal = "${stats.shotsOnTargetHome}",
                awayVal = "${stats.shotsOnTargetAway}",
                homePercent = stats.shotsOnTargetHome.toFloat() / totalTarget
            )
        }
        item {
            val totalXg = (stats.xGHome + stats.xGAway).coerceAtLeast(0.1f)
            StatComparisonRow(
                title = "Beklenen Gol (xG)",
                homeVal = "%.2f".format(stats.xGHome),
                awayVal = "%.2f".format(stats.xGAway),
                homePercent = stats.xGHome / totalXg
            )
        }
        item {
            val totalCorners = (stats.cornersHome + stats.cornersAway).coerceAtLeast(1)
            StatComparisonRow(
                title = "Köşe Vuruşu",
                homeVal = "${stats.cornersHome}",
                awayVal = "${stats.cornersAway}",
                homePercent = stats.cornersHome.toFloat() / totalCorners
            )
        }
        item {
            val totalFouls = (stats.foulsHome + stats.foulsAway).coerceAtLeast(1)
            StatComparisonRow(
                title = "Fauller",
                homeVal = "${stats.foulsHome}",
                awayVal = "${stats.foulsAway}",
                homePercent = stats.foulsHome.toFloat() / totalFouls
            )
        }
        item {
            StatComparisonRow(
                title = "Pas İsabet Oranı (%)",
                homeVal = "${stats.passAccuracyHome}%",
                awayVal = "${stats.passAccuracyAway}%",
                homePercent = stats.passAccuracyHome / 100f
            )
        }
    }
}

@Composable
fun StatComparisonRow(
    title: String,
    homeVal: String,
    awayVal: String,
    homePercent: Float
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(homeVal, fontWeight = FontWeight.Bold, color = PitchGreenLight)
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(awayVal, fontWeight = FontWeight.Bold, color = GoldAccent)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Two-tone progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(homePercent.coerceIn(0.05f, 0.95f))
                        .background(PitchGreenLight)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight((1f - homePercent).coerceIn(0.05f, 0.95f))
                        .background(GoldAccent)
                )
            }
        }
    }
}

@Composable
fun LineupsTabContent(match: MatchSummary) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Formations card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(match.homeTeam, fontWeight = FontWeight.Bold)
                        Text("Diziliş: ${match.homeFormation}", color = PitchGreenLight, style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(match.awayTeam, fontWeight = FontWeight.Bold)
                        Text("Diziliş: ${match.awayFormation}", color = GoldAccent, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Text("İlk 11 Oyuncuları", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (match.homeStarters.isNotEmpty()) {
            items(match.homeStarters.indices.toList()) { index ->
                val homePlayer = match.homeStarters.getOrNull(index)
                val awayPlayer = match.awayStarters.getOrNull(index)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (homePlayer != null) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text("${homePlayer.number}", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), color = PitchGreenLight)
                            Text(homePlayer.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("%.1f".format(homePlayer.rating), style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (awayPlayer != null) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                            Text("%.1f".format(awayPlayer.rating), style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(awayPlayer.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("${awayPlayer.number}", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End, color = GoldAccent)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
            }
        } else {
            item {
                Text(
                    text = "Bu maç için detaylı kadro bilgisi girilmemiştir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
