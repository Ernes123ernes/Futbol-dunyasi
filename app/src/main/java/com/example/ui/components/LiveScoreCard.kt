package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchSummary
import com.example.ui.theme.*

@Composable
fun LiveScoreCard(
    liveMatches: List<MatchSummary>,
    onMatchClick: (MatchSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    if (liveMatches.isEmpty()) return

    var activeIndex by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }
    val currentMatch = liveMatches.getOrNull(activeIndex.coerceIn(0, liveMatches.size - 1)) ?: liveMatches.first()

    // Pulsing live badge animation
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B16)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        PitchGreenLight.copy(alpha = 0.8f),
                        RedCardColor.copy(alpha = 0.5f),
                        PitchGreenLight.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("live_score_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF142C23),
                            Color(0xFF0B1713),
                            Color(0xFF080F0D)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Top Live Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Glowing Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = RedCardColor.copy(alpha = pulseAlpha),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Text(
                                    text = "CANLI SKOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        // Running Match Minute with Pulsing StopWatch
                        Surface(
                            color = PitchGreenLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Canlı Dakika",
                                    tint = PitchGreenLight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = currentMatch.statusText.replace("CANLI ", ""),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    ),
                                    color = PitchGreenLight
                                )
                            }
                        }
                    }

                    // Multi-live matches switch controls
                    if (liveMatches.size > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    activeIndex = if (activeIndex > 0) activeIndex - 1 else liveMatches.size - 1
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Önceki Maç",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = "${activeIndex + 1}/${liveMatches.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            IconButton(
                                onClick = {
                                    activeIndex = (activeIndex + 1) % liveMatches.size
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Sonraki Maç",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = currentMatch.league,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scoreboard Area with animated transition
                AnimatedContent(
                    targetState = currentMatch,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "live_match_content"
                ) { match ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Home Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(match.homeColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.homeTeam.take(2).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = match.homeTeam,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Big Score Box
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF071410).copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, PitchGreenLight.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${match.homeScore}",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 32.sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = ":",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = PitchGreenLight
                                        )
                                        Text(
                                            text = "${match.awayScore}",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 32.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "CANLI YAYIN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GoldAccent
                                )
                            }

                            // Away Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(match.awayColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.awayTeam.take(2).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = match.awayTeam,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Live Scorers Ticker
                        if (match.goals.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsSoccer,
                                        contentDescription = "Gol",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = match.goals.joinToString(separator = " • ") { "${it.player} ${it.minute}'" },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Compact Quick Possession Summary Bar
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%${match.stats.possessionHome} Topla Oynama",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = PitchGreenLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Şut: ${match.stats.shotsHome} - ${match.stats.shotsAway}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Topla Oynama %${match.stats.possessionAway}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        // Two-tone real-time live possession bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((match.stats.possessionHome / 100f).coerceIn(0.1f, 0.9f))
                                    .background(PitchGreenLight)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((match.stats.possessionAway / 100f).coerceIn(0.1f, 0.9f))
                                    .background(GoldAccent)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action & Expandable Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${currentMatch.venue}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Expand / Collapse Action Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpanded) GoldAccent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isExpanded) GoldAccent.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .testTag("live_score_expand_toggle_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "İstatistikleri Gizle" else "Detaylı İstatistikler",
                                tint = if (isExpanded) GoldAccent else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isExpanded) "İstatistikleri Kapat" else "Detaylı Veriler",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isExpanded) GoldAccent else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Deep match detail dialog button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PitchGreenLight,
                        modifier = Modifier
                            .clickable { onMatchClick(currentMatch) }
                            .testTag("live_score_detail_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Anlatım",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }

                // Expandable Detailed Statistics View
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(250)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .testTag("live_score_expanded_details")
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 14.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        )

                        // Section Header
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "CANLI MAÇ İSTATİSTİKLERİ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = PitchGreenLight
                                )
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Detaylı Karşılaştırma",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Team Color Indicator Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentMatch.homeColorHex))
                                )
                                Text(
                                    text = currentMatch.homeTeam,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "vs",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = currentMatch.awayTeam,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentMatch.awayColorHex))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Detailed Stats Comparison Container
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val homeAccent = Color(currentMatch.homeColorHex)
                                val awayAccent = Color(currentMatch.awayColorHex)

                                // 1. Topa Sahip Olma Oranı
                                LiveStatComparisonRow(
                                    label = "Topa Sahip Olma",
                                    homeValue = "%${currentMatch.stats.possessionHome}",
                                    awayValue = "%${currentMatch.stats.possessionAway}",
                                    homeRatio = currentMatch.stats.possessionHome.toFloat(),
                                    awayRatio = currentMatch.stats.possessionAway.toFloat(),
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_possession"
                                )

                                // 2. Toplam Şut Sayısı
                                LiveStatComparisonRow(
                                    label = "Toplam Şut",
                                    homeValue = "${currentMatch.stats.shotsHome}",
                                    awayValue = "${currentMatch.stats.shotsAway}",
                                    homeRatio = currentMatch.stats.shotsHome.toFloat(),
                                    awayRatio = currentMatch.stats.shotsAway.toFloat(),
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_shots"
                                )

                                // 3. İsabetli Şut
                                LiveStatComparisonRow(
                                    label = "İsabetli Şut",
                                    homeValue = "${currentMatch.stats.shotsOnTargetHome}",
                                    awayValue = "${currentMatch.stats.shotsOnTargetAway}",
                                    homeRatio = currentMatch.stats.shotsOnTargetHome.toFloat(),
                                    awayRatio = currentMatch.stats.shotsOnTargetAway.toFloat(),
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_shots_on_target"
                                )

                                // 4. Faul Sayısı
                                LiveStatComparisonRow(
                                    label = "Faul Sayısı",
                                    homeValue = "${currentMatch.stats.foulsHome}",
                                    awayValue = "${currentMatch.stats.foulsAway}",
                                    homeRatio = currentMatch.stats.foulsHome.toFloat(),
                                    awayRatio = currentMatch.stats.foulsAway.toFloat(),
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_fouls"
                                )

                                // 5. Korner Sayısı
                                LiveStatComparisonRow(
                                    label = "Kornerler",
                                    homeValue = "${currentMatch.stats.cornersHome}",
                                    awayValue = "${currentMatch.stats.cornersAway}",
                                    homeRatio = currentMatch.stats.cornersHome.toFloat(),
                                    awayRatio = currentMatch.stats.cornersAway.toFloat(),
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_corners"
                                )

                                // 6. Pas İsabet Oranı
                                LiveStatComparisonRow(
                                    label = "Pas İsabeti",
                                    homeValue = "%${currentMatch.stats.passAccuracyHome}",
                                    awayValue = "%${currentMatch.stats.passAccuracyAway}",
                                    homeRatio = currentMatch.stats.passAccuracyHome.toFloat(),
                                    awayRatio = currentMatch.stats.passAccuracyAway.toFloat(),
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_pass_accuracy"
                                )

                                // 7. Beklenen Gol (xG)
                                LiveStatComparisonRow(
                                    label = "Beklenen Gol (xG)",
                                    homeValue = "%.2f".format(currentMatch.stats.xGHome),
                                    awayValue = "%.2f".format(currentMatch.stats.xGAway),
                                    homeRatio = currentMatch.stats.xGHome,
                                    awayRatio = currentMatch.stats.xGAway,
                                    homeColor = homeAccent,
                                    awayColor = awayAccent,
                                    testTag = "live_score_stat_xg"
                                )
                            }
                        }

                        // Match Details Sub-footer
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hakem: ${currentMatch.referee}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "${currentMatch.round} • ${currentMatch.league}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GoldAccent
                                )
                            }
                        }

                        // Full Match Details Action Button
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onMatchClick(currentMatch) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PitchGreenLight,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_score_open_full_details_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tüm Maç Detayları, Canlı Yorumlar & Kadrolar",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveStatComparisonRow(
    label: String,
    homeValue: String,
    awayValue: String,
    homeRatio: Float,
    awayRatio: Float,
    homeColor: Color,
    awayColor: Color,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = homeColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = awayValue,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = awayColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.12f)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val total = (homeRatio + awayRatio).coerceAtLeast(0.01f)
            val homeWeight = (homeRatio / total).coerceIn(0.05f, 0.95f)
            val awayWeight = (awayRatio / total).coerceIn(0.05f, 0.95f)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeWeight)
                    .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                    .background(homeColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(awayWeight)
                    .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                    .background(awayColor)
            )
        }
    }
}

