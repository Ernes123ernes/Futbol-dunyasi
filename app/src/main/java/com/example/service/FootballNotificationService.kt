package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.MatchSummary
import com.example.data.model.NotificationPreferences

/**
 * Kullanıcının takip ettiği takımların gol atması veya maçın başlaması
 * durumunda Android sistem bildirimlerini yöneten ve tetikleyen servis mantığı.
 */
class FootballNotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "live_football_alerts_channel"
        const val CHANNEL_NAME = "Canlı Maç & Gol Bildirimleri"
        const val CHANNEL_DESC = "Takip edilen takımların gol ve maç başlangıç bildirimleri"

        const val EXTRA_MATCH_ID = "extra_target_match_id"

        @Volatile
        private var instance: FootballNotificationService? = null

        fun getInstance(context: Context): FootballNotificationService {
            return instance ?: synchronized(this) {
                instance ?: FootballNotificationService(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Takımın kullanıcının takip ettiği takımlar arasında olup olmadığını doğrular.
     */
    fun isTeamFollowed(teamName: String, preferences: NotificationPreferences): Boolean {
        if (preferences.favoriteTeam == "Tüm Takımlar" && !preferences.favoriteTeamOnly) {
            return true
        }

        val isPrimaryFav = preferences.favoriteTeam.equals(teamName, ignoreCase = true)
        val isInFollowedList = preferences.followedTeams.any { it.equals(teamName, ignoreCase = true) }

        return if (preferences.favoriteTeamOnly) {
            isPrimaryFav
        } else {
            isPrimaryFav || isInFollowedList
        }
    }

    /**
     * Takip edilen takımın maçı başladığında sistem bildirimi gönderir.
     */
    fun notifyMatchStart(
        match: MatchSummary,
        preferences: NotificationPreferences
    ): Boolean {
        if (!preferences.matchStartAlerts) {
            Log.d("NotificationService", "Match start alerts disabled in preferences")
            return false
        }

        val homeFollowed = isTeamFollowed(match.homeTeam, preferences)
        val awayFollowed = isTeamFollowed(match.awayTeam, preferences)

        if (!homeFollowed && !awayFollowed) {
            Log.d("NotificationService", "Match does not involve a followed team: ${match.homeTeam} vs ${match.awayTeam}")
            return false
        }

        val followedTeamName = when {
            homeFollowed && awayFollowed -> "${match.homeTeam} & ${match.awayTeam}"
            homeFollowed -> match.homeTeam
            else -> match.awayTeam
        }

        val title = "⏱️ Maç Başladı! ${match.homeTeam} vs ${match.awayTeam}"
        val content = "İlk düdük çaldı! Takip ettiğiniz $followedTeamName sahada. Canlı skor ve istatistikleri takip edin."

        return sendSystemNotification(
            notificationId = (match.id.hashCode() and 0xFFFF) + 1000,
            title = title,
            contentText = content,
            subText = "Maç Başlangıcı",
            matchId = match.id
        )
    }

    /**
     * Takip edilen takım gol attığında sistem bildirimi gönderir.
     */
    fun notifyGoal(
        match: MatchSummary,
        scoringTeam: String,
        scorerName: String,
        minute: Int,
        preferences: NotificationPreferences
    ): Boolean {
        if (!preferences.goalAlerts) {
            Log.d("NotificationService", "Goal alerts disabled in preferences")
            return false
        }

        if (!isTeamFollowed(scoringTeam, preferences)) {
            Log.d("NotificationService", "Scoring team '$scoringTeam' is not followed by user")
            return false
        }

        val title = "⚽ GOOOLLL! $scoringTeam"
        val content = "${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam} | $scorerName ($minute')"

        return sendSystemNotification(
            notificationId = (match.id.hashCode() and 0xFFFF) + 2000 + minute,
            title = title,
            contentText = content,
            subText = "Canlı Gol",
            matchId = match.id
        )
    }

    /**
     * Gerçek Android Bildirimi İnşa Etme ve Gönderme
     */
    fun sendSystemNotification(
        notificationId: Int,
        title: String,
        contentText: String,
        subText: String,
        matchId: String? = null
    ): Boolean {
        // Android 13+ (API 33) POST_NOTIFICATIONS izin kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "POST_NOTIFICATIONS permission not granted")
                return false
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (matchId != null) {
                putExtra(EXTRA_MATCH_ID, matchId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.i("NotificationService", "Notification delivered: $title")
            true
        } catch (e: SecurityException) {
            Log.e("NotificationService", "SecurityException posting notification", e)
            false
        }
    }
}
