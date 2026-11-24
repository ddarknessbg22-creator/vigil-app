package com.vigil.app.ui.quotes

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.Quotes
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import com.vigil.app.ui.theme.VigilIcons
import java.time.Instant
import java.time.ZoneId

@Composable
fun QuotesScreen(vm: VigilViewModel) {
    val all by vm.quotes.collectAsStateWithLifecycle()
    val reads by vm.readings.collectAsStateWithLifecycle()
    val o by vm.options.collectAsStateWithLifecycle()
    val now by vm.clock.collectAsStateWithLifecycle()
    val time = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime()

    val daily = remember(all, time.toLocalDate(), time.hour, o.category) {
        Quotes.featured(all, time, o.category)
    }
    val indexed = remember(all) { all.associateBy { it.id } }
    val categories = remember(all) { listOf("Any") + all.map { it.category }.distinct() }

    var selected by rememberSaveable { mutableStateOf("") }
    var cat by rememberSaveable { mutableStateOf("Any") }
    var sheet by rememberSaveable { mutableStateOf("") }

    val quote = indexed[selected] ?: daily
    val saved = reads.filter { it.saved }
    val c = LocalContext.current

    LaunchedEffect(quote?.id) { quote?.let { vm.seen(it.id) } }

    Page("Quotes", "V I G I L    /    Reading room") {
        quote?.let {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Kicker(if (selected.isEmpty()) "Today’s feature" else "From the library")
                    Text(
                        "“${it.text}”",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Text(
                        "VIGIL original  ·  ${it.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PillButton(
                            label = if (reads.any { r -> r.id == it.id && r.saved }) "Saved" else "Save",
                            icon = VigilIcons.heart,
                            accent = if (reads.any { r -> r.id == it.id && r.saved })
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            filled = false,
                            modifier = Modifier.weight(1f)
                        ) { vm.favorite(it.id) }

                        PillButton(
                            label = "Share",
                            icon = VigilIcons.upload,
                            accent = MaterialTheme.colorScheme.primary,
                            filled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            c.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, "“${it.text}” — VIGIL original"),
                                    "Share quote"
                                )
                            )
                        }
                    }
                }
            }
        }

        PillButton(
            label = "Discover a quote",
            icon = VigilIcons.sparkle,
            accent = MaterialTheme.colorScheme.primary,
            filled = true,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
        ) {
            val pool = all.filter { cat == "Any" || it.category == cat }
            selected = (pool.filter { it.id != quote?.id }.randomOrNull() ?: pool.firstOrNull())?.id.orEmpty()
        }

        if (selected.isNotEmpty()) {
            TextButton(onClick = { selected = "" }, modifier = Modifier.align(Alignment.End)) {
                Text("Back to today’s feature")
            }
        }

        Kicker("Browse by theme")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(categories) { name ->
                FilterChip(
                    selected = cat == name,
                    onClick = {
                        cat = name
                        selected = if (name == "Any") "" else all.firstOrNull { it.category == name }?.id.orEmpty()
                    },
                    label = { Text(name, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Kicker("More for you")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            all.filter { cat == "Any" || it.category == cat }
                .filter { it.id != quote?.id }
                .shuffled()
                .take(6)
                .forEach { other ->
                    Surface(
                        onClick = { selected = other.id },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "“${other.text}”",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                other.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
        }

        Surface(
            onClick = { sheet = "Favourites" },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(VigilIcons.heart, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Saved words", style = MaterialTheme.typography.titleSmall)
                    Text("${saved.size} quotes kept close", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(VigilIcons.chevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Surface(
            onClick = { sheet = "Reading history" },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(VigilIcons.doc, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Reading history", style = MaterialTheme.typography.titleSmall)
                    Text("${reads.size} readings", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(VigilIcons.chevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (sheet.isNotEmpty()) {
        Sheet(sheet, { sheet = "" }) {
            val items = reads.filter { sheet != "Favourites" || it.saved }
            if (items.isEmpty()) {
                Text(
                    "Nothing here yet. Save a quote and it will appear.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                var count by remember(sheet) { mutableIntStateOf(20) }
                items.take(count).forEach { r ->
                    indexed[r.id]?.let {
                        ActionRow(it.text, it.category) { selected = it.id; sheet = "" }
                    }
                }
                if (count < items.size) {
                    TextButton(onClick = { count += 20 }) { Text("Show more") }
                }
            }
        }
    }
}

@Composable
private fun PillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) accent else accent.copy(alpha = 0.14f),
            contentColor = if (filled) androidx.compose.ui.graphics.Color.White else accent
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
