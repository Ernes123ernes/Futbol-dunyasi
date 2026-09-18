package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerStatLeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreenLight

enum class StatsCategory(val label: String) {
    GOALS("Gol Krallığı"),
    ASSISTS("Asist Krallığı"),
    LEAGUE_OVERVIEW("Lig Genel")
}

@Composable
fun StatisticsScreen(
    topScorers: List<PlayerStatLeader>,
    topAssists: List<PlayerStatLeader>,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(StatsCategory.GOALS) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Switcher Tab Row
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatsCategory.values().forEachIndexed { index, cat ->
                    SegmentedButton(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsCategory.values().size)
                    ) {
                        Text(cat.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        when (selectedCategory) {
            StatsCategory.GOALS -> {
                item {
                    Text(
                        text = "2025/2026 Süper Lig Gol Krallığı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(topScorers) { leader ->
                    PlayerLeaderCard(leader = leader, statUnit = "Gol")
                }
            }
            StatsCategory.ASSISTS -> {
                item {
                    Text(
                        text = "2025/2026 Süper Lig Asist Krallığı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(topAssists) { leader ->
                    PlayerLeaderCard(leader = leader, statUnit = "Asist")
                }
            }
            StatsCategory.LEAGUE_OVERVIEW -> {
                item {
                    LeagueOverviewCard()
                }
            }
        }
    }
}

@Composable
fun PlayerLeaderCard(
    leader: PlayerStatLeader,
    statUnit: String
) {
    val rankBadgeColor = when (leader.rank) {
        1 -> GoldAccent
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leader_card_${leader.name.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Badge with Icon for Top 3
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(rankBadgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (leader.rank == 1) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = "${leader.rank}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (leader.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Player Info
                Column {
                    Text(
                        text = leader.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(leader.photoColorHex))
                        )
                        Text(
                            text = "${leader.team} • ${leader.secondaryStat}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stat Value Big Badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${leader.value}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = statUnit,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LeagueOverviewCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Lig Genel İstatistik Raporu",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem("Maç Başına Gol", "2.84", "Yüksek Verimlilik")
                MetricItem("Ev Sahibi Galibiyeti", "%48", "125/260 Maç")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem("Topla Oynama Lideri", "Galatasaray", "%59 Ortalama")
                MetricItem("En Az Gol Yiyen", "Galatasaray", "18 Gol / 26 Maç")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem("En Çok xG Üreten", "Galatasaray", "2.42 xG / Maç")
                MetricItem("Duran Top Başarısı", "Fenerbahçe", "14 Gol")
            }
        }
    }
}

@Composable
fun MetricItem(title: String, value: String, subtitle: String) {
    Column(modifier = Modifier.width(150.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PitchGreenLight)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
