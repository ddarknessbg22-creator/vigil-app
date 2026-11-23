package com.vigil.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * VIGIL's own icon set, declared once and drawn as tintable 24dp strokes.
 *
 * Every screen references `VigilIcons.x`, so no screen needs an icon import.
 * The icons are built from path data at first access instead of coming from
 * `material-icons-extended`: that keeps the APK smaller and removes the whole
 * class of "unresolved icon reference" failures.
 */
object VigilIcons {

    val shield: ImageVector by lazy { icon(PATH_SHIELD) }
    val shieldOff: ImageVector by lazy { icon(PATH_SHIELD_OFF) }
    val calendar: ImageVector by lazy { icon(PATH_CALENDAR) }
    val calendarMonth: ImageVector by lazy { calendar }
    val block: ImageVector by lazy { icon(PATH_BLOCK) }
    val checkCircle: ImageVector by lazy { icon(PATH_CHECK_CIRCLE) }
    val flame: ImageVector by lazy { icon(PATH_FLAME) }
    val security: ImageVector by lazy { icon(PATH_LOCK) }
    val science: ImageVector by lazy { icon(PATH_SPARKLE) }
    val edit: ImageVector by lazy { icon(PATH_EDIT) }
    val trendingUp: ImageVector by lazy { icon(PATH_TRENDING) }
    val dns: ImageVector by lazy { icon(PATH_DNS) }
    val root: ImageVector by lazy { icon(PATH_ROOT) }
    val list: ImageVector by lazy { icon(PATH_LIST) }
    val chevronRight: ImageVector by lazy { icon(PATH_CHEVRON_RIGHT) }
    val back: ImageVector by lazy { icon(PATH_CHEVRON_LEFT) }
    val add: ImageVector by lazy { icon(PATH_PLUS) }
    val delete: ImageVector by lazy { icon(PATH_TRASH) }
    val deleteForever: ImageVector by lazy { icon(PATH_TRASH) }
    val play: ImageVector by lazy { icon(PATH_PLAY) }
    val cancel: ImageVector by lazy { icon(PATH_CLOSE) }
    val warning: ImageVector by lazy { icon(PATH_WARNING) }
    val star: ImageVector by lazy { icon(PATH_STAR) }
    val trophy: ImageVector by lazy { icon(PATH_TROPHY) }
    val medal: ImageVector by lazy { icon(PATH_TROPHY) }
    val android: ImageVector by lazy { icon(PATH_PHONE) }
    val palette: ImageVector by lazy { icon(PATH_PALETTE) }
    val darkMode: ImageVector by lazy { icon(PATH_MOON) }
    val sparkle: ImageVector by lazy { icon(PATH_SPARKLE) }
    val accessibility: ImageVector by lazy { icon(PATH_USER) }
    val privacy: ImageVector by lazy { icon(PATH_SHIELD) }
    val device: ImageVector by lazy { icon(PATH_PHONE) }
    val noAnalytics: ImageVector by lazy { icon(PATH_TRENDING) }
    val noAccount: ImageVector by lazy { icon(PATH_USER_OFF) }
    val doc: ImageVector by lazy { icon(PATH_DOC) }
    val storage: ImageVector by lazy { icon(PATH_STORAGE) }
    val download: ImageVector by lazy { icon(PATH_DOWNLOAD) }
    val upload: ImageVector by lazy { icon(PATH_UPLOAD) }
    val restore: ImageVector by lazy { icon(PATH_RESTORE) }
    val key: ImageVector by lazy { icon(PATH_KEY) }
    val tag: ImageVector by lazy { icon(PATH_TAG) }
    val library: ImageVector by lazy { icon(PATH_LIBRARY) }
    val heart: ImageVector by lazy { icon(PATH_HEART) }
    val info: ImageVector by lazy { icon(PATH_INFO) }
    val notifications: ImageVector by lazy { icon(PATH_BELL) }
    val settings: ImageVector by lazy { icon(PATH_SETTINGS) }
    val flag: ImageVector by lazy { icon(PATH_FLAG) }
    val assessment: ImageVector by lazy { icon(PATH_TRENDING) }
    val lock: ImageVector by lazy { icon(PATH_LOCK) }
    val emergency: ImageVector by lazy { icon(PATH_WARNING) }
}

private fun icon(pathData: String): ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

private const val PATH_SHIELD =
    "M12 2.5 L19.5 5.5 V11.2 C19.5 16.3 16.2 20.1 12 21.5 C7.8 20.1 4.5 16.3 4.5 11.2 V5.5 Z"
private const val PATH_SHIELD_OFF =
    "M12 2.5 L19.5 5.5 V11.2 C19.5 16.3 16.2 20.1 12 21.5 C7.8 20.1 4.5 16.3 4.5 11.2 V5.5 Z M3.5 3.5 L20.5 20.5"
private const val PATH_CALENDAR =
    "M4.5 6.5 H19.5 V20 H4.5 Z M4.5 10.5 H19.5 M8.5 3.5 V7.5 M15.5 3.5 V7.5"
private const val PATH_BLOCK =
    "M12 2.5 A9.5 9.5 0 1 0 12 21.5 A9.5 9.5 0 1 0 12 2.5 M6.2 6.2 L17.8 17.8"
private const val PATH_CHECK_CIRCLE =
    "M12 2.5 A9.5 9.5 0 1 0 12 21.5 A9.5 9.5 0 1 0 12 2.5 M7.5 12.2 L10.8 15.5 L16.8 9"
private const val PATH_FLAME =
    "M12 21.5 C7.4 21.5 4.5 18.3 4.5 14.6 C4.5 10.6 7.8 7.8 9.2 3 C9.7 6.8 11.1 8.2 12.6 9.6 C13.1 7.2 14 5.6 14 5.6 C15.6 8.4 19.5 10.8 19.5 14.6 C19.5 18.3 16.6 21.5 12 21.5 Z"
private const val PATH_LOCK =
    "M7.5 10.8 V8 A4.5 4.5 0 0 1 16.5 8 V10.8 M5.5 10.8 H18.5 V20.5 H5.5 Z"
private const val PATH_SPARKLE =
    "M12 3 L13.7 9.1 L19.8 10.8 L13.7 12.5 L12 18.6 L10.3 12.5 L4.2 10.8 L10.3 9.1 Z"
private const val PATH_EDIT =
    "M4.5 19.5 H8 L18.6 8.9 A2.1 2.1 0 0 0 15.6 5.9 L5 16.5 Z M14.2 7.3 L16.7 9.8"
private const val PATH_TRENDING =
    "M3.5 17 L9.5 11 L13.2 14.7 L20.5 7.4 M15.5 7.4 H20.5 V12.4"
private const val PATH_DNS =
    "M4 6.5 H20 M4 12 H20 M4 17.5 H13.5"
private const val PATH_ROOT =
    "M12 2.5 L19.5 6 V12 C19.5 16.8 16.2 20.2 12 21.5 C7.8 20.2 4.5 16.8 4.5 12 V6 Z M12 8.5 V15.5 M8.5 12 H15.5"
private const val PATH_LIST = "M5 6.5 H19.5 M5 12 H19.5 M5 17.5 H13"
private const val PATH_CHEVRON_RIGHT = "M9.5 5 L16.5 12 L9.5 19"
private const val PATH_CHEVRON_LEFT = "M14.5 5 L7.5 12 L14.5 19"
private const val PATH_PLUS = "M12 5 V19 M5 12 H19"
private const val PATH_TRASH = "M4.5 7.5 H19.5 M9.5 7.5 V4.5 H14.5 V7.5 M6.8 7.5 L7.8 20 H16.2 L17.2 7.5"
private const val PATH_PLAY = "M8 5 L18.5 12 L8 19 Z"
private const val PATH_CLOSE = "M6.5 6.5 L17.5 17.5 M17.5 6.5 L6.5 17.5"
private const val PATH_WARNING =
    "M12 3.2 L21.2 20 H2.8 Z M12 9.5 V14.2 M12 17.1 V17.3"
private const val PATH_STAR =
    "M12 3.2 L14.7 9.1 L21.2 9.9 L16.4 14.2 L17.7 20.5 L12 17.3 L6.3 20.5 L7.6 14.2 L2.8 9.9 L9.3 9.1 Z"
private const val PATH_TROPHY =
    "M8 3.8 H16 V10.2 C16 13.4 14.2 15.6 12 15.6 C9.8 15.6 8 13.4 8 10.2 Z M12 15.6 V19.2 M8.2 20.6 H15.8"
private const val PATH_PHONE = "M7.5 3.2 H16.5 V20.8 H7.5 Z M10.5 18.2 H13.5"
private const val PATH_PALETTE =
    "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 M8.6 9.6 A1.1 1.1 0 1 0 8.6 9.7 M13.6 7.6 A1.1 1.1 0 1 0 13.6 7.7 M16.6 12.6 A1.1 1.1 0 1 0 16.6 12.7"
private const val PATH_MOON =
    "M20.2 14.4 A8.6 8.6 0 1 1 10 4.2 A7.1 7.1 0 0 0 20.2 14.4 Z"
private const val PATH_USER =
    "M12 11.8 A3.8 3.8 0 1 0 12 4.2 A3.8 3.8 0 1 0 12 11.8 M4.8 20.6 C4.8 17.3 8 15 12 15 C16 15 19.2 17.3 19.2 20.6"
private const val PATH_USER_OFF =
    "M12 11.8 A3.8 3.8 0 1 0 12 4.2 A3.8 3.8 0 1 0 12 11.8 M4.8 20.6 C4.8 17.3 8 15 12 15 C16 15 19.2 17.3 19.2 20.6 M3.2 3.2 L20.8 20.8"
private const val PATH_DOC =
    "M6.2 3.4 H13.6 L18.6 8.4 V20.6 H6.2 Z M13.6 3.4 V8.4 H18.6 M9.4 13.2 H15.8 M9.4 17 H14"
private const val PATH_STORAGE =
    "M4.2 8.4 H19.8 V20.2 H4.2 Z M4.2 8.4 L6.4 3.6 H17.6 L19.8 8.4 M8.4 14 H10.2 M13.8 14 H15.6"
private const val PATH_DOWNLOAD = "M12 3.5 V15 M7.2 10.4 L12 15.2 L16.8 10.4 M4.5 20 H19.5"
private const val PATH_UPLOAD = "M12 20.5 V9 M7.2 13.6 L12 8.8 L16.8 13.6 M4.5 4 H19.5"
private const val PATH_RESTORE =
    "M4 12.2 A8.2 8.2 0 1 0 12 4 A8.2 8.2 0 0 0 6.2 6.6 M3.4 4.2 V8.8 H8"
private const val PATH_KEY =
    "M14.4 10.2 A3.6 3.6 0 1 0 14.4 10.3 M14.4 10.2 H20.6 V13.4 M17.6 13.4 V16.2"
private const val PATH_TAG = "M4.2 12.4 L12.4 4.2 H20 V11.8 L11.8 20 Z M15.6 7.8 A1.2 1.2 0 1 0 15.6 7.9"
private const val PATH_LIBRARY = "M5 4.2 V19.8 M9.2 4.2 V19.8 M13.4 5.4 L17.4 19 M3.2 19.8 H19.4"
private const val PATH_HEART =
    "M12 20.2 C6.4 16.6 3.4 13.8 3.4 10.2 A4.4 4.4 0 0 1 12 7.4 A4.4 4.4 0 0 1 20.6 10.2 C20.6 13.8 17.6 16.6 12 20.2 Z"
private const val PATH_INFO = "M12 2.5 A9.5 9.5 0 1 0 12 21.5 A9.5 9.5 0 1 0 12 2.5 M12 11 V16.6 M12 7.8 V8"
private const val PATH_BELL =
    "M12 3.2 A5.6 5.6 0 0 1 17.6 8.8 V14.2 L19.4 17.2 H4.6 L6.4 14.2 V8.8 A5.6 5.6 0 0 1 12 3.2 M10 20 A2.1 2.1 0 0 0 14 20"
private const val PATH_SETTINGS =
    "M12 15.4 A3.4 3.4 0 1 0 12 8.6 A3.4 3.4 0 1 0 12 15.4 M12 2.6 V5.4 M12 18.6 V21.4 M2.6 12 H5.4 M18.6 12 H21.4 M5.4 5.4 L7.2 7.2 M16.8 16.8 L18.6 18.6 M18.6 5.4 L16.8 7.2 M7.2 16.8 L5.4 18.6"
private const val PATH_FLAG = "M6 21 V3.6 M6 4.2 H18.4 L16.2 8.2 L18.4 12.2 H6"
