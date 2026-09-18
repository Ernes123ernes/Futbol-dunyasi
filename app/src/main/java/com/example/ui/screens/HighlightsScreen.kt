package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchSummary
import com.example.ui.components.LiveScoreCard
import com.example.ui.components.MatchCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreenLight
import com.example.ui.viewmodel.MatchFilter

@Composable
fun HighlightsScreen(
    matches: List<MatchSummary>,
    currentFilter: MatchFilter,
    remindedMatchIds: Set<String> = emptySet(),
    isDarkMode: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    onToggleMatchReminder: ((String, String) -> Unit)? = null,
    onFilterSelected: (MatchFilter) -> Unit,
    onMatchClick: (MatchSummary) -> Unit,
    onWatchHighlight: (MatchSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredMatch = matches.firstOrNull()

    val filteredMatches = when (currentFilter) {
        MatchFilter.ALL -> matches
        MatchFilter.HIGHLIGHTS_ONLY -> matches.filter { it.statusText == "MS" }
        MatchFilter.LIVE -> matches.filter { it.isLive }
        MatchFilter.FIXTURES -> matches.filter { !it.isLive && it.statusText != "MS" }
    }

    val liveMatches = matches.filter { it.isLive }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Real-time Live Score Card
        if (liveMatches.isNotEmpty()) {
            item {
                LiveScoreCard(
                    liveMatches = liveMatches,
                    onMatchClick = onMatchClick
                )
            }
        }

        // Featured Highlight Hero Banner
        if (featuredMatch != null && currentFilter == MatchFilter.ALL) {
            item {
                FeaturedMatchBanner(
                    match = featuredMatch,
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme,
                    onClick = { onWatchHighlight(featuredMatch) }
                )
            }
        }

        // Match Filter Chips Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Maçlar & Video Özetleri",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MatchFilter.values()) { filter ->
                        FilterChip(
                            selected = currentFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            label = { Text(filter.label, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PitchGreenLight,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Match Cards
        if (filteredMatches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu kategoride henüz maç bulunmuyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredMatches) { match ->
                val matchTitle = "${match.homeTeam} vs ${match.awayTeam}"
                MatchCard(
                    match = match,
                    onClick = { onMatchClick(match) },
                    onWatchHighlight = { onWatchHighlight(match) },
                    isReminded = remindedMatchIds.contains(match.id),
                    onToggleReminder = if (onToggleMatchReminder != null) {
                        { onToggleMatchReminder(match.id, matchTitle) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun FeaturedMatchBanner(
    match: MatchSummary,
    isDarkMode: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("featured_match_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F2F24),
                            Color(0xFF0B1320)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Text("Haftanın Derbisi Özeti", color = GoldAccent, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (onToggleTheme != null) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { onToggleTheme() }
                                    .testTag("featured_match_theme_toggle_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Tema",
                                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isDarkMode) "Gece" else "Aydınlık",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${match.videoHighlightDuration} HD",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.summaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Play Circular Button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(PitchGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Özet İzle",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
