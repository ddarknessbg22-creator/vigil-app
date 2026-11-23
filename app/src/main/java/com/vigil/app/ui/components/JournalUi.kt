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
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.vigil.app.data.Quote
import java.time.*
import java.time.format.DateTimeFormatter

@Composable fun Page(title:String,kicker:String,content:@Composable ColumnScope.()->Unit){ Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=22.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Kicker(kicker);Text(title,fontSize=38.sp,lineHeight=44.sp,letterSpacing=(-1.5).sp,fontWeight=FontWeight.Medium);content()} }
@Composable fun Kicker(s:String){Text(s.uppercase(),style=MaterialTheme.typography.labelMedium,letterSpacing=1.4.sp,color=MaterialTheme.colorScheme.primary)}
@Composable fun Panel(content:@Composable ColumnScope.()->Unit){Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}}
@Composable fun QuoteCard(q:Quote,label:String){GlassSurface(Modifier.fillMaxWidth()){Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Kicker(label);Text("“${q.text}”",fontFamily=FontFamily.Serif,fontSize=24.sp,lineHeight=32.sp);Text("VIGIL original  ·  ${q.category}",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable fun ActionRow(title:String,detail:String="",action:()->Unit){Surface(onClick=action,shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);if(detail.isNotBlank()) Text(detail,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text("›",fontSize=24.sp)}}}
@Composable fun Sheet(title:String,close:()->Unit,content:@Composable ColumnScope.()->Unit){Dialog(onDismissRequest=close){Surface(shape=RoundedCornerShape(26.dp),color=MaterialTheme.colorScheme.surface){Column(Modifier.fillMaxWidth().heightIn(max=540.dp).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.headlineSmall);content();TextButton(onClick=close,modifier=Modifier.align(Alignment.End)){Text("Close")}}}}}
fun pickStart(c:Context,save:(Long)->Unit){val now=ZonedDateTime.now();DatePickerDialog(c,{_,y,m,d->TimePickerDialog(c,{_,h,min->val at=LocalDateTime.of(y,m+1,d,h,min).atZone(now.zone).toInstant().toEpochMilli();save(at)},now.hour,now.minute,true).show()},now.year,now.monthValue-1,now.dayOfMonth).apply {datePicker.maxDate=System.currentTimeMillis();show()} }
fun dateLabel(t:Long):String=Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
