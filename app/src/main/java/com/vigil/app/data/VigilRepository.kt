package com.vigil.app.data

import androidx.room.withTransaction
import com.vigil.app.data.datastore.PreferencesDataStore
import com.vigil.app.data.room.*
import java.time.*
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable data class Archive(val format:Int,val entries:List<Entry>,val goals:List<Goal>,val runs:List<Run>,val readings:List<Reading>,val theme:String,val category:String)
class VigilRepository(private val db:VigilDatabase,val prefs:PreferencesDataStore){
    val dao=db.journal()
    suspend fun start(at:Long,now:Long=System.currentTimeMillis())=db.withTransaction {
        require(at in 0..now && dao.latest()?.end != null || at in 0..now && dao.latest()==null){"A streak is already running."}
        require(at >= (dao.latest()?.end?:0L)){"Choose a time after your last streak."};dao.save(Run(at))
    }
    suspend fun restart(now:Long=System.currentTimeMillis())=db.withTransaction {
        val date=LocalDate.now()
        val active=dao.latest()
        require(active!=null && active.end==null && now>active.start){"Check your device time before starting again."}
        dao.save(active.copy(end=endBeforeSetback(date)))
        dao.save(Run(nextRunStart(date)))
        val day=date.toString();val entry=dao.entry(day)?:Entry(day,day,"");dao.save(entry.copy(setback=true))
    }
    suspend fun reflect(note:String)=db.withTransaction { val day=LocalDate.now().toString();val entry=dao.entry(day)?:Entry(day,day,"");dao.save(entry.copy(note=note.trim().take(4000))) }
    /** Records today's outcome. A setback closes the current clean run and begins a new one. */
    suspend fun markDay(clean:Boolean,note:String="",now:Long=System.currentTimeMillis())=db.withTransaction {
        val date=LocalDate.now()
        val day=date.toString()
        // One decision per day. Once recorded it is final — no silent retries
        // and no changing your answer later from a settings screen.
        if(dao.entry(day)!=null) return@withTransaction
        val entry=Entry(day,day,"")
        val text=note.trim().take(4000)
        if(clean){
            val active=dao.latest()
            if(active==null || active.end!=null){
                val startOfDay=date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val lastEnd=dao.runs().first().maxOfOrNull { it.end ?: 0L } ?: 0L
                val at=maxOf(startOfDay,lastEnd).coerceAtMost(now)
                dao.save(Run(at))
            }
            dao.save(entry.copy(note=text,setback=false,mood=5))
        }else{
            val active=dao.latest()
            if(active!=null && active.end==null){
                // Never let the failed day count as clean: stop the old run
                // on the day before, and begin the next run tomorrow.
                dao.save(active.copy(end=endBeforeSetback(date)))
                dao.save(Run(nextRunStart(date)))
            }else {
                dao.save(Run(nextRunStart(date)))
            }
            dao.save(entry.copy(note=text,setback=true,mood=1))
        }
    }
    suspend fun goal(title:String){require(title.isNotBlank());dao.save(Goal(UUID.randomUUID().toString(),title.trim().take(200)))}
    suspend fun read(id:String,toggle:Boolean=false)=db.withTransaction {val old=dao.reading(id);dao.save(Reading(id,System.currentTimeMillis(),if(toggle) old?.saved!=true else old?.saved==true))}
    suspend fun export():String=db.withTransaction {val o=prefs.options.first();Json.encodeToString(Archive(2,dao.entries().first(),dao.goals().first(),dao.runs().first(),dao.readings().first(),o.theme,o.category))}
    suspend fun restore(text:String,ids:Set<String>,categories:Set<String>){
        val a=Json.decodeFromString<Archive>(text);val now=System.currentTimeMillis();val sorted=a.runs.sortedBy { it.start }
        require(a.format==2 && a.theme in listOf("system","dark","light") && (a.category=="Any" || a.category in categories))
        require(a.entries.all { LocalDate.parse(it.day).toString()==it.day && it.note.length<=16000 && it.mood in 1..5 } && a.goals.all { it.title.isNotBlank() && it.target>0 })
        require(sorted.all { it.start in 0..now && (it.end==null || it.end in it.start..now) } && sorted.zipWithNext().all { (p,n)->p.start<n.start && (p.end?:Long.MAX_VALUE)<=n.start })
        require(a.readings.all { it.id in ids } && a.entries.distinctBy { it.id }.size==a.entries.size && a.goals.distinctBy { it.id }.size==a.goals.size && a.readings.distinctBy { it.id }.size==a.readings.size)
        db.withTransaction {dao.clearEntries();dao.clearGoals();dao.clearRuns();dao.clearReadings();a.entries.forEach { dao.save(it) };a.goals.forEach { dao.save(it) };sorted.forEach { dao.save(it) };a.readings.forEach { dao.save(it) }}
        prefs.setTheme(a.theme);prefs.setCategory(a.category)
    }
}
