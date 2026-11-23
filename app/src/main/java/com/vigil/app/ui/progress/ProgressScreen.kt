package com.vigil.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.*
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable fun ProgressScreen(vm:VigilViewModel){val now by vm.clock.collectAsStateWithLifecycle();val runs by vm.runs.collectAsStateWithLifecycle();val entries by vm.entries.collectAsStateWithLifecycle();val goals by vm.goals.collectAsStateWithLifecycle();val today=Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate();val active=runs.firstOrNull { it.end==null }?.elapsed(now)?.toDays()?:0L;val best=runs.maxOfOrNull { it.elapsed(now).toDays() }?:0L;var month by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) };val m=YearMonth.parse(month);val days=remember(entries){entries.map { it.day }.toSet()};var sheet by rememberSaveable { mutableStateOf("") }
    Page("Progress","V I G I L    /    Your journal"){
        Panel {Kicker("Your commitment");Text("$active days now   ·   $best best",style=MaterialTheme.typography.headlineSmall);Text(milestones.lastOrNull { best>=it }?.let { "$it-day milestone reached. That time is yours to keep." }?:"Your first milestone is a full day.")}
        Panel {Row(verticalAlignment=Alignment.CenterVertically){Text(m.format(DateTimeFormatter.ofPattern("MMMM yyyy")),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium);TextButton(onClick={month=m.minusMonths(1).toString()}){Text("Prev")};TextButton(enabled=m<YearMonth.from(today),onClick={month=m.plusMonths(1).toString()}){Text("Next")}};Row {listOf("M","T","W","T","F","S","S").forEach { Text(it,Modifier.weight(1f),textAlign=TextAlign.Center,style=MaterialTheme.typography.labelMedium) }};val offset=m.atDay(1).dayOfWeek.value-1;repeat((offset+m.lengthOfMonth()+6)/7){w->Row {repeat(7){d->val n=w*7+d-offset+1;val valid=n in 1..m.lengthOfMonth();val marked=valid && m.atDay(n).toString() in days;Box(Modifier.weight(1f).padding(1.dp).heightIn(min=30.dp).background(if(marked)MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,CircleShape),contentAlignment=Alignment.Center){if(valid)Text(n.toString(),color=if(marked)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)}}}};Text("Highlighted days are your check-ins.",style=MaterialTheme.typography.bodySmall)}
        ActionRow("Personal goals",goals.firstOrNull { !it.complete }?.title?:"Choose something worth making time for"){sheet="Personal goals"};ActionRow("Journal history","${days.size} days checked in"){sheet="Journal history"};ActionRow("Streak history","Milestones and past commitments"){sheet="Streak history"}
    }
    if(sheet!="")Sheet(sheet,{sheet=""}){when(sheet){"Personal goals"->{var title by rememberSaveable { mutableStateOf("") };OutlinedTextField(title,{title=it.take(200)},label={Text("One intention")});Button(enabled=title.isNotBlank(),onClick={vm.goal(title);title=""}){Text("Add goal")};goals.forEach { g->ActionRow(g.title,if(g.complete)"Completed · tap to reopen" else "Active · tap to complete"){vm.complete(g)} }};"Journal history"->{if(entries.isEmpty())Text("Your reflections will appear here after a check-in.");var limit by remember { mutableIntStateOf(20) };entries.take(limit).forEach { Kicker(it.day);Text(it.note.ifBlank { "Checked in" });if(it.setback)Text("A new start",style=MaterialTheme.typography.labelSmall) };if(limit<entries.size)TextButton(onClick={limit+=20}){Text("Show more")}};else->{Text("Earned milestones: "+milestones.filter { it<=best }.joinToString { "$it days" }.ifBlank { "None yet" });if(runs.isEmpty())Text("Set your starting date on Home.");runs.forEach { r->Text("${dateLabel(r.start)} — ${r.end?.let(::dateLabel)?:"today"}");Text("${r.elapsed(now).toDays()} days · ${r.elapsed(now).toHours()%24} hours",style=MaterialTheme.typography.bodyMedium) }}}}
}
