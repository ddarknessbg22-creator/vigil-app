package com.vigil.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Single place where every icon used by VIGIL is declared.
 *
 * Every other file references `VigilIcons.x` and therefore needs no icon
 * imports at all. If an icon name is wrong it fails here once, in one file,
 * instead of producing dozens of "unresolved reference" errors in the screens.
 */
object VigilIcons {
    val shield: ImageVector get() = Icons.Default.Shield
    val shieldOff: ImageVector get() = Icons.Default.ShieldOff
    val calendar: ImageVector get() = Icons.Default.CalendarToday
    val calendarMonth: ImageVector get() = Icons.Default.CalendarMonth
    val block: ImageVector get() = Icons.Default.Block
    val checkCircle: ImageVector get() = Icons.Default.CheckCircle
    val flame: ImageVector get() = Icons.Default.LocalFireDepartment
    val security: ImageVector get() = Icons.Default.Security
    val science: ImageVector get() = Icons.Default.Science
    val edit: ImageVector get() = Icons.Default.EditNote
    val trendingUp: ImageVector get() = Icons.Default.TrendingUp
    val dns: ImageVector get() = Icons.Default.Dns
    val root: ImageVector get() = Icons.Default.AdminPanelSettings
    val list: ImageVector get() = Icons.Default.ListAlt
    val chevronRight: ImageVector get() = Icons.Default.ChevronRight
    val back: ImageVector get() = Icons.Default.KeyboardArrowLeft
    val add: ImageVector get() = Icons.Default.Add
    val delete: ImageVector get() = Icons.Default.Delete
    val deleteForever: ImageVector get() = Icons.Default.DeleteForever
    val play: ImageVector get() = Icons.Default.PlayArrow
    val cancel: ImageVector get() = Icons.Default.Cancel
    val warning: ImageVector get() = Icons.Default.Warning
    val star: ImageVector get() = Icons.Default.Star
    val trophy: ImageVector get() = Icons.Default.EmojiEvents
    val medal: ImageVector get() = Icons.Default.MilitaryTech
    val android: ImageVector get() = Icons.Default.Android
    val palette: ImageVector get() = Icons.Default.Palette
    val darkMode: ImageVector get() = Icons.Default.DarkMode
    val sparkle: ImageVector get() = Icons.Default.AutoAwesome
    val accessibility: ImageVector get() = Icons.Default.Accessibility
    val privacy: ImageVector get() = Icons.Default.PrivacyTip
    val device: ImageVector get() = Icons.Default.Smartphone
    val noAnalytics: ImageVector get() = Icons.Default.Analytics
    val noAccount: ImageVector get() = Icons.Default.PersonOff
    val doc: ImageVector get() = Icons.Default.Description
    val storage: ImageVector get() = Icons.Default.Storage
    val download: ImageVector get() = Icons.Default.Download
    val upload: ImageVector get() = Icons.Default.Upload
    val restore: ImageVector get() = Icons.Default.Restore
    val key: ImageVector get() = Icons.Default.VpnKey
    val tag: ImageVector get() = Icons.Default.Tag
    val library: ImageVector get() = Icons.Default.LibraryBooks
    val heart: ImageVector get() = Icons.Default.Favorite
    val info: ImageVector get() = Icons.Default.Info
    val notifications: ImageVector get() = Icons.Default.Notifications
    val settings: ImageVector get() = Icons.Default.Settings
    val flag: ImageVector get() = Icons.Default.Flag
    val assessment: ImageVector get() = Icons.Default.Assessment
    val lock: ImageVector get() = Icons.Default.Lock
    val emergency: ImageVector get() = Icons.Default.Warning
}
