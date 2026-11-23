package com.vigil.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("vigil_prefs")
data class Options(val theme:String="system",val category:String="Any",val reminder:Boolean=false,val minute:Int=1200,val quoteNotice:Boolean=false)
class PreferencesDataStore(context:Context){
    private val store=context.store
    private val theme=stringPreferencesKey("theme_mode")
    private val category=stringPreferencesKey("quote_category")
    private val reminder=booleanPreferencesKey("gentle_reminder")
    private val minute=intPreferencesKey("reminder_minute")
    private val quote=booleanPreferencesKey("include_quote")
    val options=store.data.map { Options(it[theme]?:"system",it[category]?:"Any",it[reminder]?:false,it[minute]?:1200,it[quote]?:false) }
    suspend fun setTheme(v:String){store.edit { it[theme]=v }}
    suspend fun setCategory(v:String){store.edit { it[category]=v }}
    suspend fun notifications(enabled:Boolean,time:Int,include:Boolean){store.edit { it[reminder]=enabled;it[minute]=time;it[quote]=include }}
}
