package com.vigil.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.vigil.app.*
import com.vigil.app.data.*
import com.vigil.app.data.room.*
import com.vigil.app.data.datastore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream

class VigilViewModel(app:Application):AndroidViewModel(app){
    private val context=VigilApplication.from(app)
    private val repo=context.repository
    private val scope=SharingStarted.WhileSubscribed(0)
    val options=repo.prefs.options.stateIn(viewModelScope,scope,Options())
    val entries=repo.dao.entries().stateIn(viewModelScope,scope,emptyList())
    val runs=repo.dao.runs().stateIn(viewModelScope,scope,emptyList())
    val goals=repo.dao.goals().stateIn(viewModelScope,scope,emptyList())
    val readings=repo.dao.readings().stateIn(viewModelScope,scope,emptyList())
    val quotes=flow { emit(Quotes.load(context)) }.flowOn(Dispatchers.IO).stateIn(viewModelScope,SharingStarted.Lazily,emptyList())
    val clock=flow {while(true){val now=System.currentTimeMillis();emit(now);delay(1000-now%1000)}}.stateIn(viewModelScope,scope,System.currentTimeMillis())
    val message=MutableSharedFlow<String>(extraBufferCapacity=4)
    private fun work(success:String?=null,block:suspend ()->Unit){viewModelScope.launch {try {withContext(Dispatchers.IO){block()};success?.let { message.emit(it) }}catch(e:CancellationException){throw e}catch(e:Exception){message.emit(if(e is IllegalArgumentException && !e.message.isNullOrBlank() && e.message!!.length<100) e.message!! else "Could not complete that change. Please try again.")}}}
    fun start(at:Long)=work("Your commitment starts here."){repo.start(at)}
    fun restart()=work("Your earlier progress is safe. This is a new beginning."){repo.restart()}
    fun reflect(note:String)=work("Today’s check-in is saved."){repo.reflect(note)}
    fun goal(title:String)=work {repo.goal(title)}
    fun complete(g:Goal)=work {repo.dao.save(g.copy(complete=!g.complete))}
    fun seen(id:String)=work {repo.read(id)}
    fun favorite(id:String)=work {repo.read(id,true)}
    fun theme(s:String)=work {repo.prefs.setTheme(s)}
    fun category(s:String)=work {repo.prefs.setCategory(s)}
    fun notifications(enabled:Boolean,time:Int,quote:Boolean)=work {repo.prefs.notifications(enabled,time,quote);ReminderReceiver.schedule(context,repo.prefs.options.first())}
    fun export(uri:Uri)=work("Private backup saved."){val text=repo.export();requireNotNull(context.contentResolver.openOutputStream(uri,"wt")).bufferedWriter().use { it.write(text) }}
    fun restore(uri:Uri)=work("Journal restored. Your reminder settings are unchanged."){
        val text=requireNotNull(context.contentResolver.openInputStream(uri)).use { input->val out=ByteArrayOutputStream();val buf=ByteArray(8192);while(true){val n=input.read(buf);if(n<0)break;require(out.size()+n<=16*1024*1024){"This backup is too large."};out.write(buf,0,n)};out.toString("UTF-8") }
        val library=quotes.first { it.isNotEmpty() };repo.restore(text,library.map { it.id }.toSet(),library.map { it.category }.toSet())
    }
}
