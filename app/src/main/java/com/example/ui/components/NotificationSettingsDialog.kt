package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MatchSummary
import com.example.data.model.NotificationPreferences
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreenLight
import com.example.ui.theme.RedCardColor
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    preferences: NotificationPreferences,
    matches: List<MatchSummary>,
    remindedMatchIds: Set<String>,
    themeMode: ThemeMode = ThemeMode.DARK,
    onThemeModeChange: ((ThemeMode) -> Unit)? = null,
    onUpdatePreferences: (NotificationPreferences) -> Unit,
    onToggleMatchReminder: (matchId: String, matchTitle: String) -> Unit,
    onTriggerTestGoal: (() -> Unit)? = null,
    onTriggerTestMatchStart: (() -> Unit)? = null,
    onToggleFollowedTeam: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var goalAlerts by remember { mutableStateOf(preferences.goalAlerts) }
    var redCardAlerts by remember { mutableStateOf(preferences.redCardAlerts) }
    var matchStartAlerts by remember { mutableStateOf(preferences.matchStartAlerts) }
    var fullTimeAlerts by remember { mutableStateOf(preferences.fullTimeAlerts) }
    var highlightAlerts by remember { mutableStateOf(preferences.highlightReadyAlerts) }
    var soundAndVibration by remember { mutableStateOf(preferences.soundAndVibration) }
    var favoriteTeamOnly by remember { mutableStateOf(preferences.favoriteTeamOnly) }
    var selectedTeam by remember { mutableStateOf(preferences.favoriteTeam) }
    var showTestAlertBanner by remember { mutableStateOf(false) }

    val allSuperLigTeams = listOf(
        "Galatasaray", "Fenerbahçe", "Beşiktaş", "Trabzonspor",
        "Samsunspor", "Eyüpspor", "Göztepe", "Başakşehir", "Sivasspor"
    )

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
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Bildirimler",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Bildirim & Hatırlatıcı",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Canlı skorlar ve maç uyarıları",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_notification_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                // Simulated Instant Test Alert Banner
                AnimatedVisibility(visible = showTestAlertBanner) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = PitchGreenLight.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CANLI BİLDİRİM TESTİ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PitchGreenLight
                                )
                                Text(
                                    text = "⚽ GOL! Galatasaray 1 - 0 Fenerbahçe (Osimhen 38')",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showTestAlertBanner = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                ) {
                    // Section 1: Important Match Reminders List
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = PitchGreenLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Önemli Maç Hatırlatıcıları",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = PitchGreenLight.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${remindedMatchIds.size} Hatırlatıcı Aktif",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PitchGreenLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Hatırlatıcı kurduğunuz maçlar başlamadan 15 dakika önce ve canlı skorlarda size anlık bildirim gönderilir.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // List of matches for quick reminder toggling
                                matches.forEach { match ->
                                    val isReminded = remindedMatchIds.contains(match.id)
                                    val matchTitle = "${match.homeTeam} vs ${match.awayTeam}"

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = matchTitle,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (match.isLive) {
                                                        Surface(
                                                            color = RedCardColor,
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = match.statusText,
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${match.league} • ${match.matchDate} • ${match.round}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Reminder Toggle Button
                                            Button(
                                                onClick = { onToggleMatchReminder(match.id, matchTitle) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isReminded) PitchGreenLight else MaterialTheme.colorScheme.surface,
                                                    contentColor = if (isReminded) Color.Black else MaterialTheme.colorScheme.onSurface
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .testTag("match_reminder_btn_${match.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isReminded) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                                    contentDescription = "Hatırlatıcı Kur",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isReminded) "Kuruldu" else "Hatırlat",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section: Theme & Appearance Manager
                    if (onThemeModeChange != null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Görünüm & Tema Tercihi",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Özetleri ve canlı skorları karanlık modda izleyin",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val modes = listOf(
                                            Triple(ThemeMode.DARK, "Karanlık", Icons.Default.DarkMode),
                                            Triple(ThemeMode.LIGHT, "Aydınlık", Icons.Default.LightMode),
                                            Triple(ThemeMode.SYSTEM, "Sistem", Icons.Default.SettingsBrightness)
                                        )

                                        modes.forEach { (mode, label, icon) ->
                                            val isSelected = themeMode == mode
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PitchGreenLight else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { onThemeModeChange(mode) }
                                                    .testTag("theme_mode_chip_${mode.name.lowercase()}")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = label,
                                                        tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: Live Score Event Preferences
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "Canlı Skor Bildirim Tercihleri",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                NotificationToggleRow(
                                    title = "Canlı Gol Bildirimleri",
                                    subtitle = "Maçta gol olduğunda anında skor uyarısı al",
                                    checked = goalAlerts,
                                    onCheckedChange = {
                                        goalAlerts = it
                                        onUpdatePreferences(
                                            preferences.copy(goalAlerts = it)
                                        )
                                    },
                                    testTag = "toggle_goal_alerts"
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                NotificationToggleRow(
                                    title = "Kırmızı Kart & VAR Bildirimleri",
                                    subtitle = "Kritik hakem, penaltı ve VAR kararlarında uyar",
                                    checked = redCardAlerts,
                                    onCheckedChange = {
                                        redCardAlerts = it
                                        onUpdatePreferences(
                                            preferences.copy(redCardAlerts = it)
                                        )
                                    },
                                    testTag = "toggle_red_card_alerts"
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                NotificationToggleRow(
                                    title = "Maç Başlangıç Hatırlatıcısı (15 Dk)",
                                    subtitle = "Takip ettiğiniz maç başlamadan önce bildirim gönder",
                                    checked = matchStartAlerts,
                                    onCheckedChange = {
                                        matchStartAlerts = it
                                        onUpdatePreferences(
                                            preferences.copy(matchStartAlerts = it)
                                        )
                                    },
                                    testTag = "toggle_start_alerts"
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                NotificationToggleRow(
                                    title = "Maç Sonu & Özet Yayında",
                                    subtitle = "Karşılaşma bittiğinde ve HD özet yüklendiğinde haber ver",
                                    checked = fullTimeAlerts,
                                    onCheckedChange = {
                                        fullTimeAlerts = it
                                        onUpdatePreferences(
                                            preferences.copy(fullTimeAlerts = it)
                                        )
                                    },
                                    testTag = "toggle_fulltime_alerts"
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                NotificationToggleRow(
                                    title = "Ses & Titreşim",
                                    subtitle = "Gol ve kart bildirimlerinde telefon titreşimi ve uyarı sesi",
                                    checked = soundAndVibration,
                                    onCheckedChange = {
                                        soundAndVibration = it
                                        onUpdatePreferences(
                                            preferences.copy(soundAndVibration = it)
                                        )
                                    },
                                    testTag = "toggle_sound_vibration"
                                )
                            }
                        }
                    }

                    // Section 3: Followed Teams & Filter
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = RedCardColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Takip Edilen Takımlar",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Gol attıklarında ve maç başladığında sistem bildirimi gönderilir",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                NotificationToggleRow(
                                    title = "Yalnızca Takip Ettiğim Takımlar",
                                    subtitle = "Bildirimleri yalnızca takip listenizdeki takımlarla sınırlandırın",
                                    checked = favoriteTeamOnly,
                                    onCheckedChange = {
                                        favoriteTeamOnly = it
                                        onUpdatePreferences(
                                            preferences.copy(favoriteTeamOnly = it)
                                        )
                                    },
                                    testTag = "toggle_fav_team_only"
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                Text(
                                    text = "Bildirim Almak İstediğiniz Takımları Seçin:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Multi-select Followed Teams Chips
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(allSuperLigTeams) { team ->
                                            val isFollowed = preferences.followedTeams.contains(team) || preferences.favoriteTeam == team
                                            FilterChip(
                                                selected = isFollowed,
                                                onClick = {
                                                    if (onToggleFollowedTeam != null) {
                                                        onToggleFollowedTeam(team)
                                                    } else {
                                                        val updatedSet = preferences.followedTeams.toMutableSet()
                                                        if (updatedSet.contains(team)) updatedSet.remove(team) else updatedSet.add(team)
                                                        onUpdatePreferences(preferences.copy(followedTeams = updatedSet))
                                                    }
                                                },
                                                leadingIcon = if (isFollowed) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp),
                                                            tint = Color.Black
                                                        )
                                                    }
                                                } else null,
                                                label = { Text(team, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isFollowed) FontWeight.Bold else FontWeight.Normal)) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = PitchGreenLight,
                                                    selectedLabelColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 4: Real System Notification Test Buttons
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = PitchGreenLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Sistem Bildirimlerini Test Edin",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Durum çubuğuna gerçek Android bildirimini anında tetikleyin",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (onTriggerTestGoal != null) {
                                                onTriggerTestGoal()
                                            } else {
                                                showTestAlertBanner = true
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("test_system_goal_notification_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreenLight)
                                    ) {
                                        Text(
                                            text = "⚽ Gol Bildirimi",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (onTriggerTestMatchStart != null) {
                                                onTriggerTestMatchStart()
                                            } else {
                                                showTestAlertBanner = true
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("test_system_kickoff_notification_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "⏱️ Maç Başladı",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PitchGreenLight
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
