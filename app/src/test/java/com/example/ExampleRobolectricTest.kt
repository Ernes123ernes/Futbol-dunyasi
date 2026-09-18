package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Futbol Dünyası", appName)
  }

  @Test
  fun `theme manager toggles dark and light mode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val themeManager = ThemeManager.getInstance(context)
    
    // Set to DARK
    themeManager.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, themeManager.themeMode.value)
    
    // Toggle should switch to LIGHT
    themeManager.toggleDarkLight()
    assertEquals(ThemeMode.LIGHT, themeManager.themeMode.value)
    
    // Toggle again switches back to DARK
    themeManager.toggleDarkLight()
    assertEquals(ThemeMode.DARK, themeManager.themeMode.value)
  }

  @Test
  fun `team comparison data verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.FootballRepository(db.commentDao())
    val standings = repo.getStandings()
    
    val gs = standings.find { it.shortName == "GS" }
    val fb = standings.find { it.shortName == "FB" }
    
    org.junit.Assert.assertNotNull(gs)
    org.junit.Assert.assertNotNull(fb)
    
    // Check core comparison statistics (galibiyet, mağlubiyet, gol sayısı)
    org.junit.Assert.assertTrue(gs!!.won > 0)
    org.junit.Assert.assertTrue(fb!!.won > 0)
    org.junit.Assert.assertTrue(gs.goalsFor > 0)
    org.junit.Assert.assertTrue(fb.goalsFor > 0)
    org.junit.Assert.assertTrue(gs.points >= fb.points)
  }

  @Test
  fun `notification service respects followed teams and triggers goal alert`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationService = com.example.service.FootballNotificationService.getInstance(context)

    val prefs = com.example.data.model.NotificationPreferences(
      goalAlerts = true,
      matchStartAlerts = true,
      favoriteTeamOnly = true,
      favoriteTeam = "Galatasaray",
      followedTeams = setOf("Galatasaray", "Fenerbahçe")
    )

    // Test followed team detection
    org.junit.Assert.assertTrue(notificationService.isTeamFollowed("Galatasaray", prefs))
    org.junit.Assert.assertFalse(notificationService.isTeamFollowed("Konyaspor", prefs))

    val db = com.example.data.local.AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.FootballRepository(db.commentDao())
    val match = repo.getMatches().first()

    // Goal for followed team
    val goalDelivered = notificationService.notifyGoal(
      match = match,
      scoringTeam = "Galatasaray",
      scorerName = "Victor Osimhen",
      minute = 35,
      preferences = prefs
    )
    org.junit.Assert.assertTrue(goalDelivered)

    // Goal for non-followed team when favoriteTeamOnly is true should not notify
    val nonFollowedGoalDelivered = notificationService.notifyGoal(
      match = match,
      scoringTeam = "Konyaspor",
      scorerName = "Player X",
      minute = 50,
      preferences = prefs
    )
    org.junit.Assert.assertFalse(nonFollowedGoalDelivered)

    // Match start for followed team
    val matchStartDelivered = notificationService.notifyMatchStart(
      match = match,
      preferences = prefs
    )
    org.junit.Assert.assertTrue(matchStartDelivered)
  }
}
