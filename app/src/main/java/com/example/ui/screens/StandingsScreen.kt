package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeagueStanding
import com.example.data.model.QualificationType
import com.example.ui.theme.*

enum class StandingsFilter(val label: String) {
    ALL("Tüm Takımlar"),
    CHAMPIONS("Şampiyonlar Ligi"),
    EUROPE("Avrupa Kupaları"),
    RELEGATION("Düşme Hattı"),
    BEST_FORM("En İyi Form (Son 5)")
}

@Composable
fun StandingsScreen(
    standings: List<LeagueStanding>,
    selectedLeague: String,
    onNavigateToCompare: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(StandingsFilter.ALL) }
    var expandedTeamRank by remember { mutableStateOf<Int?>(null) }

    // Filter and sort
    val displayedStandings = remember(standings, selectedFilter) {
        when (selectedFilter) {
            StandingsFilter.ALL -> standings.sortedBy { it.rank }
            StandingsFilter.CHAMPIONS -> standings.filter { it.qualification == QualificationType.CHAMPIONS_LEAGUE }
            StandingsFilter.EUROPE -> standings.filter {
                it.qualification == QualificationType.CHAMPIONS_LEAGUE ||
                it.qualification == QualificationType.EUROPA_LEAGUE ||
                it.qualification == QualificationType.CONFERENCE_LEAGUE
            }
            StandingsFilter.RELEGATION -> standings.filter { it.qualification == QualificationType.RELEGATION }
            StandingsFilter.BEST_FORM -> standings.sortedByDescending { standing ->
                calculateFormPoints(standing.formLast5)
            }
        }
    }

    val leader = standings.minByOrNull { it.rank }
    val topScoringTeam = standings.maxByOrNull { it.goalsFor }
    val bestFormTeam = standings.maxByOrNull { calculateFormPoints(it.formLast5) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("standings_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 1. Screen Header & Week
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$selectedLeague Puan Durumu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Son 5 maçlık performans grafikleri doğrudan tabloda listelenmektedir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PitchGreenLight.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "26. Hafta",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PitchGreenLight
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. League Quick Highlights Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Leader Card
                leader?.let {
                    HighlightMetricCard(
                        title = "Lider",
                        mainText = it.shortName,
                        subText = "${it.points} Puan",
                        accentColor = Color(it.primaryColorHex),
                        icon = Icons.Default.EmojiEvents,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Top Scoring Team
                topScoringTeam?.let {
                    HighlightMetricCard(
                        title = "En Çok Gol",
                        mainText = it.shortName,
                        subText = "${it.goalsFor} Gol",
                        accentColor = GoldAccent,
                        icon = Icons.Default.SportsSoccer,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Best Form Team
                bestFormTeam?.let {
                    val pts = calculateFormPoints(it.formLast5)
                    HighlightMetricCard(
                        title = "En İyi Form",
                        mainText = it.shortName,
                        subText = "$pts/15 Puan",
                        accentColor = PitchGreenLight,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(StandingsFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PitchGreenLight,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 4. Standings Table Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(22.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Takım",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1.4f)
                    )
                    Text(
                        text = "O",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "G",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(22.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "B",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(22.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "M",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(22.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "AV",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "P",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center,
                        color = PitchGreenLight
                    )
                    Text(
                        text = "Son 5 Maç Trendi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(76.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 5. Standings Table Rows with Direct Line Chart Visualizations
        items(displayedStandings, key = { it.rank }) { standing ->
            val isExpanded = expandedTeamRank == standing.rank
            StandingRowWithSparkline(
                standing = standing,
                isExpanded = isExpanded,
                onClick = {
                    expandedTeamRank = if (isExpanded) null else standing.rank
                },
                onNavigateToCompare = onNavigateToCompare
            )
        }

        // 6. Qualification Zone Legend Card
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lig Kuralları & Katılım Bölgeleri",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LegendItem(color = Color(0xFF2563EB), text = "1-2: UEFA Şampiyonlar Ligi")
                            LegendItem(color = GoldAccent, text = "3: UEFA Avrupa Ligi")
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LegendItem(color = PitchGreenLight, text = "4: UEFA Konferans Ligi")
                            LegendItem(color = RedCardColor, text = "17-19: Küme Düşme Hattı")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grafik Açıklaması: Yeşil Nokta = Galibiyet (3P), Sarı = Beraberlik (1P), Kırmızı = Mağlubiyet (0P)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Puan Durumu Satırı ve Doğrudan Çizgi Grafik (Line Chart / Sparkline)
 */
@Composable
fun StandingRowWithSparkline(
    standing: LeagueStanding,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onNavigateToCompare: ((String) -> Unit)? = null
) {
    val zoneColor = when (standing.qualification) {
        QualificationType.CHAMPIONS_LEAGUE -> Color(0xFF2563EB)
        QualificationType.EUROPA_LEAGUE -> GoldAccent
        QualificationType.CONFERENCE_LEAGUE -> PitchGreenLight
        QualificationType.RELEGATION -> RedCardColor
        QualificationType.NORMAL -> Color.Transparent
    }

    val teamColor = Color(standing.primaryColorHex)
    val formPoints = calculateFormPoints(standing.formLast5)

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) teamColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .animateContentSize()
            .testTag("standing_row_${standing.rank}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zone colored left indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(zoneColor)
                )

                Spacer(modifier = Modifier.width(3.dp))

                // Rank Number
                Text(
                    text = "${standing.rank}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(20.dp),
                    textAlign = TextAlign.Center,
                    color = if (standing.rank <= 3) PitchGreenLight else MaterialTheme.colorScheme.onSurface
                )

                // Team Name & Logo Badge
                Row(
                    modifier = Modifier.weight(1.4f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(teamColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = standing.shortName.take(1),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp
                            ),
                            color = Color.White
                        )
                    }
                    Text(
                        text = standing.teamName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Stats Columns: O, G, B, M, AV, P
                Text(
                    text = "${standing.played}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${standing.won}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.width(22.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${standing.drawn}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(22.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${standing.lost}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(22.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (standing.goalDiff > 0) "+${standing.goalDiff}" else "${standing.goalDiff}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (standing.goalDiff > 0) PitchGreenLight else if (standing.goalDiff < 0) RedCardColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${standing.points}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PitchGreenLight,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )

                // Doğrudan Tabloda Gösterilen Son 5 Maç Performans Çizgi Grafiği (Sparkline)
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(32.dp)
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FormLineChartSparkline(
                        form = standing.formLast5,
                        teamColor = teamColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Genişletilebilir Detaylı Performans & Form İnceleme Paneli
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${standing.teamName} - Son 5 Maçlık Form Grafiği",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = teamColor
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PitchGreenLight.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Form Puanı: $formPoints / 15",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PitchGreenLight
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Büyük ve Detaylı 5-Maçlık Performans Çizgi Grafiği
                    DetailedFormLineChart(
                        form = standing.formLast5,
                        teamColor = teamColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    // Match by match breakdown row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        standing.formLast5.forEachIndexed { idx, result ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${idx + 1}. Maç",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FormBadgeSmall(result = result)
                                Text(
                                    text = when (result) {
                                        'G' -> "3 Puan"
                                        'B' -> "1 Puan"
                                        'M' -> "0 Puan"
                                        else -> "-"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = when (result) {
                                        'G' -> Color(0xFF16A34A)
                                        'B' -> Color(0xFFCA8A04)
                                        'M' -> Color(0xFFDC2626)
                                        else -> Color.Gray
                                    }
                                )
                            }
                        }
                    }

                    // Quick Stats & Compare Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Atılan: ${standing.goalsFor} | Yenilen: ${standing.goalsAgainst}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (onNavigateToCompare != null) {
                            Button(
                                onClick = { onNavigateToCompare(standing.teamName) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PitchGreenLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Takımı Kıyasla",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tablo İçinde Doğrudan Gösterilen Mini Performans Çizgi Grafiği (Sparkline Line Chart)
 * 5 maçlık puan trendini (3, 1, 0) pürüzsüz çizgi ve veri noktalarıyla çizer.
 */
@Composable
fun FormLineChartSparkline(
    form: List<Char>,
    teamColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (form.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val paddingX = 4f
        val paddingY = 4f
        val usableW = w - paddingX * 2
        val usableH = h - paddingY * 2

        val pointsCount = form.size
        val stepX = if (pointsCount > 1) usableW / (pointsCount - 1) else 0f

        // Convert char to point y: G -> 3, B -> 1, M -> 0
        // h=0 is top (max points = 3), h=max is bottom (min points = 0)
        val coords = form.mapIndexed { index, char ->
            val value = when (char) {
                'G' -> 3f
                'B' -> 1f
                'M' -> 0f
                else -> 1.5f
            }
            // normalized 0..1 where 3 is top (y=0) and 0 is bottom (y=usableH)
            val normY = 1f - (value / 3f)
            Offset(
                x = paddingX + index * stepX,
                y = paddingY + normY * usableH
            )
        }

        // 1. Draw gradient area fill under the line chart
        val fillPath = Path().apply {
            moveTo(coords.first().x, h)
            lineTo(coords.first().x, coords.first().y)

            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cX = (p0.x + p1.x) / 2f
                cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
            }

            lineTo(coords.last().x, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    teamColor.copy(alpha = 0.35f),
                    teamColor.copy(alpha = 0.05f)
                ),
                startY = paddingY,
                endY = h
            )
        )

        // 2. Draw line stroke connecting the matches
        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cX = (p0.x + p1.x) / 2f
                cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
            }
        }

        drawPath(
            path = strokePath,
            color = teamColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Draw colored circles for each match outcome
        coords.forEachIndexed { i, pt ->
            val char = form[i]
            val dotColor = when (char) {
                'G' -> Color(0xFF16A34A) // Green for Win
                'B' -> Color(0xFFCA8A04) // Amber for Draw
                'M' -> Color(0xFFDC2626) // Red for Loss
                else -> Color.Gray
            }

            // Outer white ring
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = pt,
                style = Fill
            )
            // Inner colored dot
            drawCircle(
                color = dotColor,
                radius = 2.dp.toPx(),
                center = pt,
                style = Fill
            )
        }
    }
}

/**
 * Genişletilmiş ve Kılavuz Çizgili Detaylı 5-Maçlık Performans Çizgi Grafiği
 */
@Composable
fun DetailedFormLineChart(
    form: List<Char>,
    teamColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        val w = size.width
        val h = size.height
        val stepX = w / 4f

        // Draw horizontal grid lines for 3P, 1P, 0P
        val y3 = 0f
        val y1 = h * (2f / 3f)
        val y0 = h

        // Grid lines
        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, y3),
            end = Offset(w, y3),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, y1),
            end = Offset(w, y1),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, y0),
            end = Offset(w, y0),
            strokeWidth = 1.dp.toPx()
        )

        // Calculate points
        val coords = form.mapIndexed { idx, char ->
            val yPos = when (char) {
                'G' -> y3
                'B' -> y1
                'M' -> y0
                else -> y1
            }
            Offset(x = idx * stepX, y = yPos)
        }

        // Gradient fill
        val fillPath = Path().apply {
            moveTo(coords.first().x, h)
            lineTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cX = (p0.x + p1.x) / 2f
                cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
            }
            lineTo(coords.last().x, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    teamColor.copy(alpha = 0.4f),
                    teamColor.copy(alpha = 0.05f)
                ),
                startY = 0f,
                endY = h
            )
        )

        // Line
        val linePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cX = (p0.x + p1.x) / 2f
                cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
            }
        }

        drawPath(
            path = linePath,
            color = teamColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Points
        coords.forEachIndexed { i, pt ->
            val char = form[i]
            val dotColor = when (char) {
                'G' -> Color(0xFF16A34A)
                'B' -> Color(0xFFCA8A04)
                'M' -> Color(0xFFDC2626)
                else -> Color.Gray
            }

            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
            drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun FormBadgeSmall(result: Char) {
    val bgColor = when (result) {
        'G' -> Color(0xFF16A34A)
        'B' -> Color(0xFFCA8A04)
        'M' -> Color(0xFFDC2626)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$result",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            ),
            color = Color.White
        )
    }
}

@Composable
fun HighlightMetricCard(
    title: String,
    mainText: String,
    subText: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun calculateFormPoints(form: List<Char>): Int {
    return form.sumOf {
        when (it) {
            'G' -> 3
            'B' -> 1
            else -> 0
        }.toInt()
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

