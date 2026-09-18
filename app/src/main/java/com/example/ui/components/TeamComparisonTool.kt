package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeagueStanding
import com.example.data.model.TeamAnalysis
import com.example.ui.theme.*

data class ChartStatItem(
    val title: String,
    val valueA: Int,
    val valueB: Int,
    val suffix: String = "",
    val lowerIsBetter: Boolean = false
)

@Composable
fun TeamComparisonTool(
    standings: List<LeagueStanding>,
    analyses: List<TeamAnalysis>,
    teamAId: String,
    teamBId: String,
    onSelectTeams: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Find matching standings or fallbacks
    val allTeams = remember(standings, analyses) {
        if (standings.isNotEmpty()) {
            standings
        } else {
            analyses.map {
                LeagueStanding(
                    rank = 1,
                    teamName = it.teamName,
                    shortName = it.shortName,
                    primaryColorHex = it.primaryColorHex,
                    played = 26,
                    won = 18,
                    drawn = 4,
                    lost = 4,
                    goalsFor = 50,
                    goalsAgainst = 25,
                    goalDiff = 25,
                    points = 58,
                    formLast5 = listOf('G', 'G', 'B', 'M', 'G'),
                    qualification = com.example.data.model.QualificationType.NORMAL
                )
            }
        }
    }

    // Resolve team A and team B
    val teamA = remember(allTeams, teamAId) {
        allTeams.find {
            it.teamName.equals(teamAId, ignoreCase = true) ||
            it.shortName.equals(teamAId, ignoreCase = true) ||
            it.teamName.contains(teamAId, ignoreCase = true)
        } ?: allTeams.firstOrNull() ?: LeagueStanding(
            1, "Galatasaray", "GS", 0xFFA90432, 26, 21, 4, 1, 62, 18, 44, 67, listOf('G', 'G', 'G', 'B', 'G'), com.example.data.model.QualificationType.CHAMPIONS_LEAGUE
        )
    }

    val teamB = remember(allTeams, teamBId) {
        allTeams.find {
            (it.teamName.equals(teamBId, ignoreCase = true) ||
            it.shortName.equals(teamBId, ignoreCase = true) ||
            it.teamName.contains(teamBId, ignoreCase = true)) &&
            it.teamName != teamA.teamName
        } ?: allTeams.getOrNull(1) ?: allTeams.firstOrNull() ?: LeagueStanding(
            2, "Fenerbahçe", "FB", 0xFF002D62, 26, 19, 4, 3, 58, 22, 36, 61, listOf('G', 'G', 'B', 'G', 'M'), com.example.data.model.QualificationType.CHAMPIONS_LEAGUE
        )
    }

    val colorA = Color(teamA.primaryColorHex)
    val colorB = Color(teamB.primaryColorHex)

    var showTeamAPicker by remember { mutableStateOf(false) }
    var showTeamBPicker by remember { mutableStateOf(false) }
    var selectedMetricFilter by remember { mutableIntStateOf(0) } // 0: Tümü, 1: Temel, 2: Goller, 3: Puan/Averaj

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("team_comparison_tool"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tool Header with Derby Quick Presets
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = PitchGreenLight.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = PitchGreenLight,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Takım Karşılaştırma Aracı",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Galibiyet, mağlubiyet ve gol sayılarını grafiklerle kıyaslayın",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick Matchup Presets
                Text(
                    text = "Hızlı Derbi Seçimi:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        Triple("GS ⚡ FB", "Galatasaray", "Fenerbahçe"),
                        Triple("BJK ⚡ GS", "Beşiktaş", "Galatasaray"),
                        Triple("TS ⚡ FB", "Trabzonspor", "Fenerbahçe"),
                        Triple("BJK ⚡ FB", "Beşiktaş", "Fenerbahçe"),
                        Triple("SAM ⚡ GS", "Samsunspor", "Galatasaray"),
                        Triple("IBFK ⚡ BJK", "Başakşehir", "Beşiktaş")
                    )
                    items(presets) { preset ->
                        val isCurrent = (teamA.teamName == preset.second && teamB.teamName == preset.third) ||
                                (teamA.teamName == preset.third && teamB.teamName == preset.second)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) PitchGreenLight.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isCurrent) PitchGreenLight else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable { onSelectTeams(preset.second, preset.third) }
                                .testTag("derby_preset_${preset.first}")
                        ) {
                            Text(
                                text = preset.first,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isCurrent) PitchGreenLight else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Team Selection Arena
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Team A Selector Box
                    TeamPickBox(
                        team = teamA,
                        isFirst = true,
                        onClick = { showTeamAPicker = true },
                        modifier = Modifier.weight(1f),
                        testTag = "team_a_selector_box"
                    )

                    // Middle Swap & VS indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    // Swap team A and B
                                    onSelectTeams(teamB.teamName, teamA.teamName)
                                }
                                .testTag("swap_teams_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Takımları Değiştir",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = GoldAccent
                        )
                    }

                    // Team B Selector Box
                    TeamPickBox(
                        team = teamB,
                        isFirst = false,
                        onClick = { showTeamBPicker = true },
                        modifier = Modifier.weight(1f),
                        testTag = "team_b_selector_box"
                    )
                }
            }
        }

        // 3. Yan Yana Sütun Grafiği (Side-by-Side Graphical Bar Chart)
        SideBySideBarChartCard(
            teamA = teamA,
            teamB = teamB,
            colorA = colorA,
            colorB = colorB
        )

        // 4. Detaylı Karşılaştırmalı Oran Çubukları (Comparative Metric Dual-Bars)
        ComparativeMetricsCard(
            teamA = teamA,
            teamB = teamB,
            colorA = colorA,
            colorB = colorB
        )

        // 5. Son 5 Maç Form Karşılaştırması
        FormComparisonCard(
            teamA = teamA,
            teamB = teamB,
            colorA = colorA,
            colorB = colorB
        )

        // 6. Kafa Kafaya Üstünlük Özeti Tablosu
        HeadToHeadSummaryTableCard(
            teamA = teamA,
            teamB = teamB,
            colorA = colorA,
            colorB = colorB
        )
    }

    // Team A Selector Dialog
    if (showTeamAPicker) {
        TeamSelectionDialog(
            title = "1. Takımı Seçin",
            teams = allTeams,
            selectedTeamName = teamA.teamName,
            onSelect = {
                onSelectTeams(it.teamName, teamB.teamName)
                showTeamAPicker = false
            },
            onDismiss = { showTeamAPicker = false }
        )
    }

    // Team B Selector Dialog
    if (showTeamBPicker) {
        TeamSelectionDialog(
            title = "2. Takımı Seçin",
            teams = allTeams,
            selectedTeamName = teamB.teamName,
            onSelect = {
                onSelectTeams(teamA.teamName, it.teamName)
                showTeamBPicker = false
            },
            onDismiss = { showTeamBPicker = false }
        )
    }
}

@Composable
private fun TeamPickBox(
    team: LeagueStanding,
    isFirst: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val teamColor = Color(team.primaryColorHex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.5.dp, teamColor.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Team Badge Circle with Rank
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(teamColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = team.shortName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = GoldAccent,
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${team.rank}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }

            Text(
                text = team.teamName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            // Team Quick Stats Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${team.points} Puan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        ),
                        color = teamColor
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${team.won}G ${team.lost}M",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Click to Change hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Değiştir",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Yan Yana Sütun Grafiği Bileşeni (Side-by-Side Graphical Bar Chart)
 * Galibiyet, Mağlubiyet, Gol Sayısı (Atılan), Yenilen Gol, Beraberlik ve Puanı sütun grafiklerle kıyaslar.
 */
@Composable
fun SideBySideBarChartCard(
    teamA: LeagueStanding,
    teamB: LeagueStanding,
    colorA: Color,
    colorB: Color
) {
    val statsList = remember(teamA, teamB) {
        listOf(
            ChartStatItem("Galibiyet", teamA.won, teamB.won),
            ChartStatItem("Mağlubiyet", teamA.lost, teamB.lost, lowerIsBetter = true),
            ChartStatItem("Atılan Gol", teamA.goalsFor, teamB.goalsFor),
            ChartStatItem("Yenilen Gol", teamA.goalsAgainst, teamB.goalsAgainst, lowerIsBetter = true),
            ChartStatItem("Beraberlik", teamA.drawn, teamB.drawn),
            ChartStatItem("Puan", teamA.points, teamB.points)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("side_by_side_barchart_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = PitchGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Temel İstatistikler Sütun Grafiği",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PitchGreenLight.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Yan Yana Kıyas",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PitchGreenLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team A Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colorA)
                    )
                    Text(
                        text = teamA.teamName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Team B Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colorB)
                    )
                    Text(
                        text = teamB.teamName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // The Graphical Bar Chart (Yan yana sütunlar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    statsList.forEach { item ->
                        val maxInPair = maxOf(item.valueA, item.valueB, 1).toFloat()
                        // Global ceiling for visual balancing
                        val maxCategory = when (item.title) {
                            "Puan", "Atılan Gol" -> 75f
                            "Galibiyet", "Yenilen Gol" -> 35f
                            else -> 30f
                        }

                        val ratioA = (item.valueA / maxCategory).coerceIn(0.08f, 1.0f)
                        val ratioB = (item.valueB / maxCategory).coerceIn(0.08f, 1.0f)

                        val animatedRatioA by animateFloatAsState(
                            targetValue = ratioA,
                            animationSpec = tween(600),
                            label = "barA_${item.title}"
                        )
                        val animatedRatioB by animateFloatAsState(
                            targetValue = ratioB,
                            animationSpec = tween(600),
                            label = "barB_${item.title}"
                        )

                        val isAWinner = if (item.lowerIsBetter) item.valueA < item.valueB else item.valueA > item.valueB
                        val isBWinner = if (item.lowerIsBetter) item.valueB < item.valueA else item.valueB > item.valueA

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                        ) {
                            // Side-by-side vertical bars container
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Bar A
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    // Value Label
                                    Text(
                                        text = "${item.valueA}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isAWinner) FontWeight.Black else FontWeight.Normal
                                        ),
                                        color = if (isAWinner) colorA else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .fillMaxHeight(fraction = animatedRatioA)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        colorA,
                                                        colorA.copy(alpha = 0.7f)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Bar B
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    // Value Label
                                    Text(
                                        text = "${item.valueB}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isBWinner) FontWeight.Black else FontWeight.Normal
                                        ),
                                        color = if (isBWinner) colorB else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .fillMaxHeight(fraction = animatedRatioB)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        colorB,
                                                        colorB.copy(alpha = 0.7f)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            // Category Label
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detaylı Karşılaştırmalı Dağılım Çubukları
 * Galibiyet Oranı, Mağlubiyet Oranı, Maç Başına Gol, Net Averaj ve Puan
 */
@Composable
fun ComparativeMetricsCard(
    teamA: LeagueStanding,
    teamB: LeagueStanding,
    colorA: Color,
    colorB: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Oransal Karşılaştırma Grafikleri",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "26 Maç Üzerinden",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 1. Galibiyet Oranı & Sayısı
            val winRateA = (teamA.won * 100f / teamA.played.coerceAtLeast(1))
            val winRateB = (teamB.won * 100f / teamB.played.coerceAtLeast(1))
            MetricDualProgressBar(
                title = "Galibiyet Sayısı & Oranı",
                valueA = "${teamA.won} G (%${winRateA.toInt()})",
                valueB = "${teamB.won} G (%${winRateB.toInt()})",
                weightA = teamA.won.toFloat(),
                weightB = teamB.won.toFloat(),
                colorA = colorA,
                colorB = colorB
            )

            // 2. Mağlubiyet Sayısı & Oranı
            val lossRateA = (teamA.lost * 100f / teamA.played.coerceAtLeast(1))
            val lossRateB = (teamB.lost * 100f / teamB.played.coerceAtLeast(1))
            MetricDualProgressBar(
                title = "Mağlubiyet Sayısı & Oranı",
                valueA = "${teamA.lost} M (%${lossRateA.toInt()})",
                valueB = "${teamB.lost} M (%${lossRateB.toInt()})",
                weightA = teamA.lost.toFloat(),
                weightB = teamB.lost.toFloat(),
                colorA = colorA,
                colorB = colorB,
                highlightLower = true
            )

            // 3. Atılan Gol Sayısı & Maç Başı Ortalaması
            val gPerMatchA = teamA.goalsFor.toFloat() / teamA.played.coerceAtLeast(1)
            val gPerMatchB = teamB.goalsFor.toFloat() / teamB.played.coerceAtLeast(1)
            MetricDualProgressBar(
                title = "Atılan Gol & Maç Başına Gol",
                valueA = "${teamA.goalsFor} (${"%.2f".format(gPerMatchA)})",
                valueB = "${teamB.goalsFor} (${"%.2f".format(gPerMatchB)})",
                weightA = teamA.goalsFor.toFloat(),
                weightB = teamB.goalsFor.toFloat(),
                colorA = colorA,
                colorB = colorB
            )

            // 4. Yenilen Gol & Maç Başına Yenilen
            val gaPerMatchA = teamA.goalsAgainst.toFloat() / teamA.played.coerceAtLeast(1)
            val gaPerMatchB = teamB.goalsAgainst.toFloat() / teamB.played.coerceAtLeast(1)
            MetricDualProgressBar(
                title = "Yenilen Gol & Savunma Ortalaması",
                valueA = "${teamA.goalsAgainst} (${"%.2f".format(gaPerMatchA)})",
                valueB = "${teamB.goalsAgainst} (${"%.2f".format(gaPerMatchB)})",
                weightA = teamA.goalsAgainst.toFloat(),
                weightB = teamB.goalsAgainst.toFloat(),
                colorA = colorA,
                colorB = colorB,
                highlightLower = true
            )

            // 5. Net Averaj
            MetricDualProgressBar(
                title = "Genel Averaj (+/- Gol Farkı)",
                valueA = if (teamA.goalDiff > 0) "+${teamA.goalDiff}" else "${teamA.goalDiff}",
                valueB = if (teamB.goalDiff > 0) "+${teamB.goalDiff}" else "${teamB.goalDiff}",
                weightA = (teamA.goalDiff + 50).coerceAtLeast(1).toFloat(),
                weightB = (teamB.goalDiff + 50).coerceAtLeast(1).toFloat(),
                colorA = colorA,
                colorB = colorB
            )

            // 6. Toplam Puan
            MetricDualProgressBar(
                title = "Toplam Puan Durumu",
                valueA = "${teamA.points} Puan",
                valueB = "${teamB.points} Puan",
                weightA = teamA.points.toFloat(),
                weightB = teamB.points.toFloat(),
                colorA = colorA,
                colorB = colorB
            )
        }
    }
}

@Composable
private fun MetricDualProgressBar(
    title: String,
    valueA: String,
    valueB: String,
    weightA: Float,
    weightB: Float,
    colorA: Color,
    colorB: Color,
    highlightLower: Boolean = false
) {
    val total = (weightA + weightB).coerceAtLeast(0.1f)
    val ratioA = (weightA / total).coerceIn(0.1f, 0.9f)
    val ratioB = 1f - ratioA

    val isAWinner = if (highlightLower) weightA < weightB else weightA > weightB
    val isBWinner = if (highlightLower) weightB < weightA else weightB > weightA

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isAWinner) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = colorA, modifier = Modifier.size(13.dp))
                }
                Text(
                    text = valueA,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isAWinner) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = colorA
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = valueB,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isBWinner) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = colorB
                )
                if (isBWinner) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = colorB, modifier = Modifier.size(13.dp))
                }
            }
        }

        // Dual progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(ratioA)
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .background(colorA)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(ratioB)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(colorB)
            )
        }
    }
}

/**
 * Son 5 Maç Form Karşılaştırması Kartı
 */
@Composable
fun FormComparisonCard(
    teamA: LeagueStanding,
    teamB: LeagueStanding,
    colorA: Color,
    colorB: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Son 5 Maç Form Karşılaştırması",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "G: Galibiyet • B: Beraberlik • M: Mağlubiyet",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Team A Form Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = teamA.teamName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorA
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    teamA.formLast5.forEach { char ->
                        FormBadge(result = char)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Team B Form Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = teamB.teamName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorB
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    teamB.formLast5.forEach { char ->
                        FormBadge(result = char)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormBadge(result: Char) {
    val bgColor = when (result) {
        'G' -> Color(0xFF16A34A) // Green
        'B' -> Color(0xFFCA8A04) // Yellow/Amber
        'M' -> Color(0xFFDC2626) // Red
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$result",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            ),
            color = Color.White
        )
    }
}

/**
 * Detaylı Kafa Kafaya Karşılaştırma Tablosu
 */
@Composable
fun HeadToHeadSummaryTableCard(
    teamA: LeagueStanding,
    teamB: LeagueStanding,
    colorA: Color,
    colorB: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Özet İstatistik Tablosu",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = teamA.shortName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = colorA,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = teamB.shortName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = colorB,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End
                )
            }

            // Table Rows
            val tableRows = listOf(
                Triple("${teamA.rank}.", "Lig Sıralaması", "${teamB.rank}."),
                Triple("${teamA.played}", "Oynanan Maç", "${teamB.played}"),
                Triple("${teamA.won}", "Galibiyet", "${teamB.won}"),
                Triple("${teamA.drawn}", "Beraberlik", "${teamB.drawn}"),
                Triple("${teamA.lost}", "Mağlubiyet", "${teamB.lost}"),
                Triple("${teamA.goalsFor}", "Atılan Gol", "${teamB.goalsFor}"),
                Triple("${teamA.goalsAgainst}", "Yenilen Gol", "${teamB.goalsAgainst}"),
                Triple("${if (teamA.goalDiff > 0) "+${teamA.goalDiff}" else "${teamA.goalDiff}"}", "Averaj", "${if (teamB.goalDiff > 0) "+${teamB.goalDiff}" else "${teamB.goalDiff}"}"),
                Triple("${teamA.points}", "Toplam Puan", "${teamB.points}")
            )

            tableRows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.first,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorA,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = row.second,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = row.third,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorB,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                }
                if (index < tableRows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
            }
        }
    }
}

/**
 * Tüm Lig Takımlarını Listeyen Seçim Diyaloğu
 */
@Composable
fun TeamSelectionDialog(
    title: String,
    teams: List<LeagueStanding>,
    selectedTeamName: String,
    onSelect: (LeagueStanding) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(teams) { team ->
                        val isSelected = team.teamName == selectedTeamName
                        val teamColor = Color(team.primaryColorHex)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) teamColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) teamColor else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(team) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(teamColor)
                                    )
                                    Column {
                                        Text(
                                            text = team.teamName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${team.rank}. Sırada • ${team.points} Puan",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçildi",
                                        tint = teamColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
