package com.vigil.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.vigil.app.data.Quote
import java.time.*
import java.time.format.DateTimeFormatter

@Composable fun Page(title:String,kicker:String,content:@Composable ColumnScope.()->Unit){
    val scroll=rememberScrollState();val d=LocalDensity.current
    val topPx=with(d){18.dp.toPx()};val rangePx=with(d){56.dp.toPx()}
    val collapse=((scroll.value-topPx)/rangePx).coerceIn(0f,1f)
    Box(Modifier.fillMaxSize()){
        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            Spacer(Modifier.height(38.dp))
            Column(Modifier.graphicsLayer{alpha=1f-collapse;translationY=-scroll.value*0.22f},verticalArrangement=Arrangement.spacedBy(2.dp)){Text(kicker,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(title,fontSize=38.sp,lineHeight=44.sp,letterSpacing=(-1).sp,fontWeight=FontWeight.Bold)}
            content();Spacer(Modifier.height(28.dp))
        }
        // Collapsed title only appears once the large title has actually
        // scrolled away, and uses the same surface colour as the cards.
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha=0.96f*collapse)),
            contentAlignment=Alignment.CenterStart
        ){
            Text(
                title,
                Modifier.graphicsLayer{alpha=collapse}.padding(start=20.dp),
                style=MaterialTheme.typography.titleMedium,
                fontWeight=FontWeight.SemiBold,
                color=MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Composable fun Kicker(s:String){Text(s,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)}
@Composable fun Panel(content:@Composable ColumnScope.()->Unit){Surface(shape=RoundedCornerShape(28.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}}
@Composable fun QuoteCard(q:Quote,label:String){Surface(shape=RoundedCornerShape(28.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Kicker(label);Text("“${q.text}”",fontFamily=FontFamily.Default,fontSize=23.sp,lineHeight=31.sp,fontWeight=FontWeight.Medium);Text("VIGIL original  ·  ${q.category}",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable fun ActionRow(title:String,detail:String="",action:()->Unit){Surface(onClick=action,shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(horizontal=20.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);if(detail.isNotBlank()) Text(detail,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text("›",fontSize=26.sp,color=MaterialTheme.colorScheme.outline)}}}
@Composable fun Sheet(title:String,close:()->Unit,content:@Composable ColumnScope.()->Unit){Dialog(onDismissRequest=close){Surface(shape=RoundedCornerShape(32.dp),color=MaterialTheme.colorScheme.surfaceContainerHigh){Column(Modifier.fillMaxWidth().heightIn(max=540.dp).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.headlineSmall);content();TextButton(onClick=close,modifier=Modifier.align(Alignment.End)){Text("Close")}}}}}
fun pickStart(c:Context,save:(Long)->Unit){val now=ZonedDateTime.now();DatePickerDialog(c,{_,y,m,d->TimePickerDialog(c,{_,h,min->val at=LocalDateTime.of(y,m+1,d,h,min).atZone(now.zone).toInstant().toEpochMilli();save(at)},now.hour,now.minute,true).show()},now.year,now.monthValue-1,now.dayOfMonth).apply {datePicker.maxDate=System.currentTimeMillis();show()} }
fun dateLabel(t:Long):String=Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
