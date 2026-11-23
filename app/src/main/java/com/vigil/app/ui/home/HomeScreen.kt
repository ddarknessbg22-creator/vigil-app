package com.vigil.app.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.*
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable fun HomeScreen(vm:VigilViewModel){
    val now by vm.clock.collectAsStateWithLifecycle();val time=Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault());val today=time.toLocalDate();val runs by vm.runs.collectAsStateWithLifecycle();val run=runs.firstOrNull { it.end==null };val elapsed=run?.elapsed(now)?:Duration.ZERO
    val entries by vm.entries.collectAsStateWithLifecycle();val check=remember(entries,today){entries.firstOrNull { it.day==today.toString() }};val all by vm.quotes.collectAsStateWithLifecycle();val o by vm.options.collectAsStateWithLifecycle();val q=remember(all,today,time.hour,o.category){Quotes.featured(all,time.toLocalDateTime(),o.category)};LaunchedEffect(q?.id){q?.let { vm.seen(it.id) }}
    var sheet by rememberSaveable { mutableStateOf(false) };val c=LocalContext.current
    Page("Today","V I G I L    /    ${today.format(DateTimeFormatter.ofPattern("EEE, d MMM"))}"){
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1E4FD8),Color(0xFF3B7BFF),Color(0xFF7FA8FF))),RoundedCornerShape(36.dp)).padding(28.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("CURRENT STREAK",color=Color(0xFFD6E4FF),letterSpacing=1.7.sp,style=MaterialTheme.typography.labelMedium)
            val shown by animateIntAsState(if(run==null) 0 else elapsed.toDays().toInt(),tween(900),label="streak")
            Text(if(run==null) "—" else shown.toString(),fontSize=84.sp,lineHeight=90.sp,fontWeight=FontWeight.Light,letterSpacing=(-4).sp,color=Color.White)
            Text(if(run==null) "Your starting point" else if(elapsed.toDays()==1L) "day of commitment" else "days of commitment",color=Color.White,style=MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp));if(run!=null){Text("${elapsed.toHours()%24} hours   ·   ${elapsed.toMinutes()%60} minutes",color=Color(0xFFEAF1FF));LinearProgressIndicator(progress={ (elapsed.toMinutes()%1440)/1440f },modifier=Modifier.fillMaxWidth().padding(top=8.dp),color=Color.White,trackColor=Color.White.copy(alpha=.18f))} else Text("Choose when your commitment began.",color=Color.White)
        }
        q?.let { QuoteCard(it,if(time.hour in 5..17) "Morning note" else "Evening note") }
        Button(onClick={if(run==null)pickStart(c,vm::start)else sheet=true},modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),shape=RoundedCornerShape(28.dp)){Text(if(run==null) "Set my start date" else if(check==null) "Check in for today" else "View today’s check-in")}
    }
    if(sheet){var note by rememberSaveable { mutableStateOf(check?.note.orEmpty()) };Sheet("A moment for today",{sheet=false}){Text(if(time.hour in 5..17) "What would you like to make room for today?" else "What helped you stay close to your intention?");OutlinedTextField(note,{note=it.take(4000)},label={Text("Private reflection (optional)")},minLines=3);Button(onClick={vm.reflect(note);sheet=false}){Text(if(check==null) "Keep my commitment" else "Save reflection")}}}
}
