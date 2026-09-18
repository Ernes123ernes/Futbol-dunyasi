package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
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
import com.example.data.model.TimelineEventType
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun NewsHighlightsVideoPlayer(
    matches: List<MatchSummary>,
    onMatchClick: (MatchSummary) -> Unit,
    isDarkMode: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val highlightMatches = remember(matches) {
        matches.filter { it.videoHighlightDuration.isNotBlank() }
    }

    if (highlightMatches.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentMatch = highlightMatches.getOrNull(selectedIndex.coerceIn(0, highlightMatches.size - 1)) ?: highlightMatches.first()

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var progressPercent by remember { mutableFloatStateOf(0.18f) }
    var activeMomentText by remember { mutableStateOf("Özeti izlemek için oynat butonuna dokunun") }
    var currentVideoSecond by remember { mutableIntStateOf(45) }
    val totalVideoSeconds = 390 // ~06:30 min

    // Simulated active playback timer
    LaunchedEffect(isPlaying, currentMatch.id) {
        if (isPlaying) {
            while (isPlaying) {
                delay(1000)
                currentVideoSecond = (currentVideoSecond + 2) % totalVideoSeconds
                progressPercent = currentVideoSecond.toFloat() / totalVideoSeconds.toFloat()
                
                // Match simulated commentary based on timeline
                val currentMinSimulated = ((currentVideoSecond.toFloat() / totalVideoSeconds.toFloat()) * 90).toInt()
                val nearestEvent = currentMatch.timeline.minByOrNull { kotlin.math.abs(it.minute - currentMinSimulated) }
                if (nearestEvent != null && kotlin.math.abs(nearestEvent.minute - currentMinSimulated) <= 8) {
                    activeMomentText = "${nearestEvent.minute}' ${nearestEvent.title}: ${nearestEvent.description}"
                } else {
                    activeMomentText = "Dakika ${currentMinSimulated}' - Karşılaşmada tempo yüksek, ataklar devam ediyor..."
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("news_highlights_video_player")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Section title and HD badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PitchGreenLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "Maç Özeti",
                            tint = PitchGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Maç Özetleri & Kritik Anlar",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Haber akışı içinde video özetler",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "HD 1080p",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match Selector Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(highlightMatches) { index, match ->
                    val isSelected = index == selectedIndex
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PitchGreenLight else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                selectedIndex = index
                                isPlaying = true
                                currentVideoSecond = 10
                                activeMomentText = "${match.homeTeam} - ${match.awayTeam} maç özeti başlatıldı"
                            }
                            .testTag("highlight_match_chip_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${match.homeTeam.take(3).uppercase()} vs ${match.awayTeam.take(3).uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${match.homeScore}-${match.awayScore}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isSelected) Color(0xFF06351F) else PitchGreenLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Canvas Box with animated content transition on match change
            AnimatedContent(
                targetState = currentMatch,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "highlight_player_content"
            ) { match ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(205.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F261E),
                                    Color(0xFF07120E),
                                    Color(0xFF040A07)
                                )
                            )
                        )
                        .border(1.dp, PitchGreenLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Video Overlay: Team Names, Score and Sound Mute Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = match.homeTeam,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${match.homeScore} - ${match.awayScore}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = PitchGreenLight
                                    )
                                    Text(
                                        text = match.awayTeam,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (onToggleTheme != null) {
                                    IconButton(
                                        onClick = onToggleTheme,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .testTag("video_player_theme_toggle_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDarkMode) "Aydınlık Moda Geç" else "Karanlık Sinema Moduna Geç",
                                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFE2E8F0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Outlined.VolumeMute else Icons.Outlined.VolumeUp,
                                        contentDescription = if (isMuted) "Sesi Aç" else "Sesi Kapat",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onMatchClick(match) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .testTag("expand_highlight_fullscreen_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.OpenInFull,
                                        contentDescription = "Detaylı İzle",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Center Play / Pause Action Button
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(PitchGreenLight)
                                    .testTag("video_player_play_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Durdur" else "Oynat",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Bottom Video Controls & Time Scrubber
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Subtitle of active moment
                            Text(
                                text = activeMomentText,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive Scrubber Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val currentMin = currentVideoSecond / 60
                                val currentSec = currentVideoSecond % 60
                                Text(
                                    text = String.format("%02d:%02d", currentMin, currentSec),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White.copy(alpha = 0.7f)
                                )

                                Slider(
                                    value = progressPercent,
                                    onValueChange = {
                                        progressPercent = it
                                        currentVideoSecond = (it * totalVideoSeconds).toInt()
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = PitchGreenLight,
                                        activeTrackColor = PitchGreenLight,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(18.dp)
                                        .testTag("video_scrubber_slider")
                                )

                                Text(
                                    text = match.videoHighlightDuration,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Jump to Key Moments (Goller, Kırmızı Kart, VAR, Kurtarışlar)
            Text(
                text = "⚡ Kritik Anlara Git:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currentMatch.timeline) { event ->
                    val icon = when (event.type) {
                        TimelineEventType.GOAL -> "⚽"
                        TimelineEventType.RED_CARD -> "🟥"
                        TimelineEventType.YELLOW_CARD -> "🟨"
                        TimelineEventType.VAR_DECISION -> "📺"
                        TimelineEventType.CHANCE -> "🧤"
                        TimelineEventType.SUBSTITUTION -> "🔄"
                        else -> "⚡"
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                isPlaying = true
                                // Calculate simulated jump second
                                currentVideoSecond = ((event.minute / 90f) * totalVideoSeconds).toInt().coerceIn(5, totalVideoSeconds - 10)
                                progressPercent = currentVideoSecond.toFloat() / totalVideoSeconds.toFloat()
                                activeMomentText = "${event.minute}' ${event.title}: ${event.description}"
                            }
                            .testTag("quick_moment_${event.minute}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = icon, fontSize = 12.sp)
                            Text(
                                text = "${event.minute}' ${event.title}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full Screen / Match Stats Action Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${currentMatch.venue} • ${currentMatch.round}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = { onMatchClick(currentMatch) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("open_match_detail_from_video_btn")
                ) {
                    Text(
                        text = "Maç İstatistikleri & Kadrolar",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PitchGreenLight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PitchGreenLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
