package com.vigil.app.data

import android.content.Context
import java.time.LocalDateTime
import java.util.UUID

data class Quote(val id:String,val category:String,val text:String,val source:String="")
object Quotes {
    fun load(c:Context):List<Quote> = buildList {
        for (file in c.assets.list("quotes").orEmpty().sorted()) {
            val lines=c.assets.open("quotes/$file").bufferedReader().use { it.readLines() };val category=lines.first().trim()
            lines.drop(1).flatMap { it.split('|') }.map { it.trim() }.filter { it.isNotEmpty() }.forEach { raw ->
                val parts = raw.split(" ~~ ", limit = 2)
                val text = parts[0].trim()
                val source = if (parts.size > 1) parts[1].trim() else ""
                add(Quote(UUID.nameUUIDFromBytes(raw.toByteArray()).toString(),category,text,source))
            }
        }
    }
    fun featured(all:List<Quote>,time:LocalDateTime,preference:String):Quote? {
        val evening=time.hour>=18 || time.hour<5
        val pool=all.filter { it.category==if(preference!="Any") preference else if(evening) "Night motivation" else "Morning motivation" }.ifEmpty { all }
        if(pool.isEmpty()) return null
        val day= time.minusHours(5).toLocalDate().toEpochDay();return pool[Math.floorMod(day+(if(evening && preference!="Any") pool.size/2 else 0),pool.size.toLong()).toInt()]
    }
}
