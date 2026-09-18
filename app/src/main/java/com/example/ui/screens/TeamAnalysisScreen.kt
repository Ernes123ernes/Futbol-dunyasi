package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeagueStanding
import com.example.data.model.TeamAnalysis
import com.example.ui.components.TeamComparisonTool
import com.example.ui.theme.*

@Composable
fun TeamAnalysisScreen(
    analyses: List<TeamAnalysis>,
    selectedAnalysis: TeamAnalysis?,
    compareTeamAId: String,
    compareTeamBId: String,
    onSelectTeam: (TeamAnalysis) -> Unit,
    onSetCompareTeams: (String, String) -> Unit,
    standings: List<LeagueStanding> = emptyList(),
    modifier: Modifier = Modifier
) {
    var isComparisonMode by remember { mutableStateOf(true) }
    val currentAnalysis = selectedAnalysis ?: analyses.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Mode Switcher Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Takım Karşılaştırma Tab
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isComparisonMode) PitchGreenLight else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isComparisonMode = true }
                                .testTag("tab_team_comparison")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = if (isComparisonMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Takım Karşılaştırma",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isComparisonMode) FontWeight.Black else FontWeight.SemiBold
                                    ),
                                    color = if (isComparisonMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Taktik & Analiz Tab
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isComparisonMode) PitchGreenLight else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isComparisonMode = false }
                                .testTag("tab_tactical_analysis")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = if (!isComparisonMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Taktik & Kadro",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (!isComparisonMode) FontWeight.Black else FontWeight.SemiBold
                                    ),
                                    color = if (!isComparisonMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isComparisonMode) {
            // Dedicated Takım Karşılaştırma Aracı
            item {
                TeamComparisonTool(
                    standings = standings,
                    analyses = analyses,
                    teamAId = compareTeamAId,
                    teamBId = compareTeamBId,
                    onSelectTeams = onSetCompareTeams
                )
            }
        } else {
            // Taktik & Kadro Analizi
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kulüp Taktik Tahtası & Kadro Verileri",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(analyses) { team ->
                            val isSelected = currentAnalysis?.teamId == team.teamId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectTeam(team) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(team.primaryColorHex))
                                        )
                                        Text(team.teamName, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(team.primaryColorHex),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            if (currentAnalysis != null) {
                // Tactical Pitch Board Representation
                item {
                    TacticalBoardCard(analysis = currentAnalysis)
                }

                // Ratings Metrics Overview
                item {
                    TacticalMetricsCard(analysis = currentAnalysis)
                }

                // Strengths and Weaknesses
                item {
                    StrengthsAndWeaknessesCard(analysis = currentAnalysis)
                }

                // Tactical In-depth Insights
                item {
                    TacticalInsightsCard(analysis = currentAnalysis)
                }
            }
        }
    }
}

@Composable
fun TacticalBoardCard(analysis: TeamAnalysis) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tactical_board_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = analysis.teamName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Teknik Direktör: ${analysis.manager}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = analysis.formation,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini Pitch Drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D3B2E))
            ) {
                SoccerPitchCanvas()
                TacticalFormationOverlay(teamColor = Color(analysis.primaryColorHex), formation = analysis.formation)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tactical Style summary
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Taktiksel Kimlik: ${analysis.tacticalStyle}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SoccerPitchCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pitchLineColor = Color.White.copy(alpha = 0.35f)
        val stroke = Stroke(width = 2.dp.toPx())

        // Pitch Outline
        drawRect(color = pitchLineColor, size = Size(w, h), style = stroke)

        // Halfway Line
        drawLine(color = pitchLineColor, start = Offset(0f, h / 2), end = Offset(w, h / 2), strokeWidth = 2.dp.toPx())

        // Center Circle
        drawCircle(color = pitchLineColor, radius = 30.dp.toPx(), center = Offset(w / 2, h / 2), style = stroke)

        // Top Penalty Box (Away side)
        val boxWidth = w * 0.55f
        val boxHeight = h * 0.22f
        drawRect(
            color = pitchLineColor,
            topLeft = Offset((w - boxWidth) / 2, 0f),
            size = Size(boxWidth, boxHeight),
            style = stroke
        )

        // Bottom Penalty Box (Home side)
        drawRect(
            color = pitchLineColor,
            topLeft = Offset((w - boxWidth) / 2, h - boxHeight),
            size = Size(boxWidth, boxHeight),
            style = stroke
        )
    }
}

@Composable
fun TacticalFormationOverlay(teamColor: Color, formation: String) {
    // Render 11 player nodes arranged on the pitch
    Box(modifier = Modifier.fillMaxSize()) {
        // Goalkeeper
        PlayerDot(teamColor = teamColor, label = "KL", x = 0.5f, y = 0.90f)

        // Back line (4 defenders)
        PlayerDot(teamColor = teamColor, label = "SOL", x = 0.16f, y = 0.74f)
        PlayerDot(teamColor = teamColor, label = "STP", x = 0.38f, y = 0.76f)
        PlayerDot(teamColor = teamColor, label = "STP", x = 0.62f, y = 0.76f)
        PlayerDot(teamColor = teamColor, label = "SAĞ", x = 0.84f, y = 0.74f)

        // Midfield Pivot (2 players)
        PlayerDot(teamColor = teamColor, label = "DOS", x = 0.35f, y = 0.58f)
        PlayerDot(teamColor = teamColor, label = "DOS", x = 0.65f, y = 0.58f)

        // Attacking Midfield (3 players)
        PlayerDot(teamColor = teamColor, label = "SOL", x = 0.18f, y = 0.38f)
        PlayerDot(teamColor = teamColor, label = "OOS", x = 0.50f, y = 0.36f)
        PlayerDot(teamColor = teamColor, label = "SAĞ", x = 0.82f, y = 0.38f)

        // Striker
        PlayerDot(teamColor = teamColor, label = "FV", x = 0.50f, y = 0.18f)
    }
}

@Composable
fun BoxScope.PlayerDot(teamColor: Color, label: String, x: Float, y: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 300.dp * x - 14.dp, y = 200.dp * y - 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(teamColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TacticalMetricsCard(analysis: TeamAnalysis) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Taktiksel Performans Göstergeleri",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            MetricProgressRow("Hücum Derecesi", analysis.attackRating, 100, PitchGreenLight)
            MetricProgressRow("Savunma Direnci", analysis.defenseRating, 100, Color(0xFF38BDF8))
            MetricProgressRow("Topa Sahip Olma (%)", analysis.possessionAvg, 100, GoldAccent)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Maç Başına xG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.2f".format(analysis.xGPerMatch), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PitchGreenLight)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pres Yoğunluğu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(analysis.pressingIntensity, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = GoldAccent)
                }
            }
        }
    }
}

@Composable
fun MetricProgressRow(title: String, current: Int, max: Int, barColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text("$current / $max", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = barColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / max },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StrengthsAndWeaknessesCard(analysis: TeamAnalysis) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Strengths
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PitchGreenLight, modifier = Modifier.size(18.dp))
                Text("Güçlü Yönler", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PitchGreenLight)
            }
            analysis.strengths.forEach { str ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", color = PitchGreenLight, fontWeight = FontWeight.Bold)
                    Text(str, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Weaknesses
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RedCardColor, modifier = Modifier.size(18.dp))
                Text("Gelişime Açık Yönler", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = RedCardColor)
            }
            analysis.weaknesses.forEach { wkn ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", color = RedCardColor, fontWeight = FontWeight.Bold)
                    Text(wkn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun TacticalInsightsCard(analysis: TeamAnalysis) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Uzman Taktiksel Görüşü", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                text = analysis.tacticalInsights,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Kilit Oyuncular:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                analysis.keyPlayers.forEach { player ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "⭐ $player",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeadToHeadDerbyComparator(
    analyses: List<TeamAnalysis>,
    teamAId: String,
    teamBId: String,
    onSetCompare: (String, String) -> Unit
) {
    val teamA = analyses.find { it.teamId == teamAId } ?: analyses.first()
    val teamB = analyses.find { it.teamId == teamBId } ?: analyses.getOrNull(1) ?: analyses.first()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Derbi Karşılaştırma & Güç Dengesi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Selectors row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team A Selector
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(teamA.teamName, fontWeight = FontWeight.Bold, color = Color(teamA.primaryColorHex))
                    Text("(${teamA.formation})", style = MaterialTheme.typography.labelSmall)
                }

                Text("VS", fontWeight = FontWeight.ExtraBold, color = GoldAccent, modifier = Modifier.padding(horizontal = 8.dp))

                // Team B Selector
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(teamB.teamName, fontWeight = FontWeight.Bold, color = Color(teamB.primaryColorHex))
                    Text("(${teamB.formation})", style = MaterialTheme.typography.labelSmall)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Metrics comparisons
            H2hBarComparison("Hücum Gücü", teamA.attackRating, teamB.attackRating, Color(teamA.primaryColorHex), Color(teamB.primaryColorHex))
            H2hBarComparison("Savunma Gücü", teamA.defenseRating, teamB.defenseRating, Color(teamA.primaryColorHex), Color(teamB.primaryColorHex))
            H2hBarComparison("Topa Sahip Olma (%)", teamA.possessionAvg, teamB.possessionAvg, Color(teamA.primaryColorHex), Color(teamB.primaryColorHex))
            H2hBarComparison("Beklenen Gol (xG)", (teamA.xGPerMatch * 30).toInt(), (teamB.xGPerMatch * 30).toInt(), Color(teamA.primaryColorHex), Color(teamB.primaryColorHex), labelA = "%.2f".format(teamA.xGPerMatch), labelB = "%.2f".format(teamB.xGPerMatch))
        }
    }
}

@Composable
fun H2hBarComparison(
    title: String,
    valA: Int,
    valB: Int,
    colorA: Color,
    colorB: Color,
    labelA: String = "$valA",
    labelB: String = "$valB"
) {
    val total = (valA + valB).coerceAtLeast(1)
    val ratioA = valA.toFloat() / total

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(labelA, fontWeight = FontWeight.Bold, color = colorA, style = MaterialTheme.typography.bodySmall)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(labelB, fontWeight = FontWeight.Bold, color = colorB, style = MaterialTheme.typography.bodySmall)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(ratioA.coerceIn(0.1f, 0.9f))
                    .background(colorA)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - ratioA).coerceIn(0.1f, 0.9f))
                    .background(colorB)
            )
        }
    }
}
