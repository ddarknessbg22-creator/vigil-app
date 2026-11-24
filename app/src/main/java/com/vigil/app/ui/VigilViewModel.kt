package com.vigil.app.ui

import android.app.Application
import com.vigil.app.data.AppUpdate
import com.vigil.app.data.UpdateChecker
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
    private val updater=UpdateChecker(context)
    private val _update: MutableStateFlow<AppUpdate?> = MutableStateFlow(null)
    val update: StateFlow<AppUpdate?> = _update.asStateFlow()
    private val scope=SharingStarted.WhileSubscribed(0)
    val options=repo.prefs.options.stateIn(viewModelScope,scope,Options())
    val entries=repo.dao.entries().stateIn(viewModelScope,scope,emptyList())
    val runs=repo.dao.runs().stateIn(viewModelScope,scope,emptyList())
    val goals=repo.dao.goals().stateIn(viewModelScope,scope,emptyList())
    val readings=repo.dao.readings().stateIn(viewModelScope,scope,emptyList())
    val quotes=flow {
        // Yield before the heavy asset read so the first frame isn't blocked.
        delay(250)
        emit(Quotes.load(context))
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope,SharingStarted.Lazily,emptyList())
    val clock=flow {while(true){val now=System.currentTimeMillis();emit(now);delay(60_000-now%60_000)}}.stateIn(viewModelScope,scope,System.currentTimeMillis())
    val message=MutableSharedFlow<String>(extraBufferCapacity=4)
    private fun work(success:String?=null,block:suspend ()->Unit){viewModelScope.launch {try {withContext(Dispatchers.IO){block()};StreakWidgetProvider.refresh(getApplication());success?.let { message.emit(it) }}catch(e:CancellationException){throw e}catch(e:Exception){message.emit(if(e is IllegalArgumentException && !e.message.isNullOrBlank() && e.message!!.length<100) e.message!! else "Could not complete that change. Please try again.")}}}
    fun start(at:Long)=work("Your commitment starts here."){repo.start(at)}
    fun restart()=work("Your earlier progress is safe. This is a new beginning."){repo.restart()}
    fun reflect(note:String)=work("Today’s check-in is saved."){repo.reflect(note)}
    fun checkUpdate(manual: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = updater.check()) {
            is UpdateResult.Available -> _update.value = result.update
            is UpdateResult.UpToDate -> {
                _update.value = null
                if (manual) message.emit("You’re up to date — v${com.vigil.app.BuildConfig.VERSION_NAME}.")
            }
            is UpdateResult.Failed -> {
                if (manual) message.emit("Update check failed: ${result.reason}")
            }
        }
    }
    fun skipUpdate(){_update.value=null}

    data class DownloadProgress(val done: Long, val total: Long)
    private val _progress = MutableStateFlow<DownloadProgress?>(null)
    val progress: StateFlow<DownloadProgress?> = _progress.asStateFlow()
    private var downloadJob: Job? = null

    fun downloadAndInstall(update:AppUpdate) {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
            try {
                _progress.value = DownloadProgress(0, 0)
                val file = updater.download(update.url) { done, total ->
                    _progress.value = DownloadProgress(done, total)
                }
                _progress.value = null
                try {
                    getApplication<Application>().startActivity(updater.installIntent(file))
                } catch (e: Exception) {
                    message.emit("Allow VIGIL to install unknown apps in Settings, then try again.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _progress.value = null
                message.emit("Could not download the update. Check your connection.")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _progress.value = null
    }
    fun checkIn(clean:Boolean,note:String,message:String?=null)=work(
        message ?: if(clean) "Today is logged clean." else "Setback saved. This is a new start."
    ){repo.markDay(clean,note)}
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
