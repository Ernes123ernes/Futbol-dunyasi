package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PitchGreenLight
import com.example.ui.viewmodel.FootballTab

data class BottomNavItem(
    val tab: FootballTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun FootballBottomNavigationBar(
    currentTab: FootballTab,
    onTabSelected: (FootballTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(FootballTab.HIGHLIGHTS, Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
        BottomNavItem(FootballTab.NEWS, Icons.Filled.Article, Icons.Outlined.Article),
        BottomNavItem(FootballTab.STANDINGS, Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered),
        BottomNavItem(FootballTab.STATS, Icons.Filled.BarChart, Icons.Outlined.BarChart),
        BottomNavItem(FootballTab.ANALYSIS, Icons.Filled.Analytics, Icons.Outlined.Analytics)
    )

    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("football_bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.tab.title
                    )
                },
                label = {
                    Text(
                        text = item.tab.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${item.tab.name.lowercase()}")
            )
        }
    }
}
