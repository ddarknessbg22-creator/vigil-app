package com.vigil.app.ui.quotes

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.*
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import java.time.*

@Composable fun QuotesScreen(vm:VigilViewModel){val all by vm.quotes.collectAsStateWithLifecycle();val reads by vm.readings.collectAsStateWithLifecycle();val o by vm.options.collectAsStateWithLifecycle();val now by vm.clock.collectAsStateWithLifecycle();val time=Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime();val daily=remember(all,time.toLocalDate(),time.hour,o.category){Quotes.featured(all,time,o.category)};val indexed=remember(all){all.associateBy { it.id }};var selected by rememberSaveable { mutableStateOf("") };var cat by rememberSaveable { mutableStateOf("Any") };var sheet by rememberSaveable { mutableStateOf("") };val q=indexed[selected]?:daily;val c=LocalContext.current;LaunchedEffect(q?.id){q?.let { vm.seen(it.id) }}
    Page("Quotes","V I G I L    /    Reading room"){
        if(q!=null){QuoteCard(q,if(selected.isEmpty())"Today’s feature" else "From the library");Row{TextButton(onClick={vm.favorite(q.id)}){Text(if(reads.any { it.id==q.id && it.saved }) "Unsave" else "Save quote")};TextButton(onClick={c.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"“${q.text}” — VIGIL original"),"Share quote"))}){Text("Share")};if(selected.isNotEmpty())TextButton(onClick={selected=""}){Text("Daily feature")}}}
        Button(enabled=all.isNotEmpty(),onClick={val pool=all.filter { cat=="Any" || it.category==cat };selected=(pool.filter { it.id!=q?.id }.randomOrNull()?:pool.first()).id},modifier=Modifier.fillMaxWidth()){Text("Discover a quote")};ActionRow("Category",cat){sheet="Category"};ActionRow("Favourites","Your saved words"){sheet="Favourites"};ActionRow("Reading history"){sheet="Reading history"}
    }
    if(sheet.isNotEmpty())Sheet(sheet,{sheet=""}){if(sheet=="Category"){(listOf("Any")+all.map { it.category }.distinct()).forEach { name->ActionRow(name){cat=name;if(name!="Any")selected=all.first { it.category==name }.id;sheet=""} }}else{val items=reads.filter { sheet!="Favourites" || it.saved };if(items.isEmpty())Text("Save a quote you want to come back to.");var count by remember(sheet){mutableIntStateOf(20)};items.take(count).forEach { r->indexed[r.id]?.let { ActionRow(it.text,it.category){selected=it.id;sheet=""} }};if(count<items.size)TextButton(onClick={count+=20}){Text("Show more")}}}
}
