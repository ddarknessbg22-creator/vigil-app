package com.vigil.app.data.room

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities=[Entry::class,Goal::class,Run::class,Reading::class],version=2,exportSchema=true)
abstract class VigilDatabase:RoomDatabase(){
    abstract fun journal():JournalDao
    companion object {
        fun getInstance(context:Context)= Room.databaseBuilder(context,VigilDatabase::class.java,"vigil_database").addMigrations(object:Migration(1,2){
            override fun migrate(db:SupportSQLiteDatabase){
                db.execSQL("CREATE TABLE journal (id TEXT NOT NULL PRIMARY KEY, day TEXT NOT NULL, note TEXT NOT NULL, mood INTEGER NOT NULL, urge INTEGER NOT NULL, setback INTEGER NOT NULL)")
                db.execSQL("INSERT INTO journal SELECT id,strftime('%Y-%m-%d',date/1000,'unixepoch','localtime'),notes,mood,hadUrge,hadRelapse FROM check_ins")
                db.execSQL("CREATE TABLE intentions (id TEXT NOT NULL PRIMARY KEY,title TEXT NOT NULL,description TEXT NOT NULL,target INTEGER NOT NULL,complete INTEGER NOT NULL)")
                db.execSQL("INSERT INTO intentions SELECT id,title,description,targetDays,isCompleted FROM goals")
                db.execSQL("CREATE TABLE runs (start INTEGER NOT NULL PRIMARY KEY,end INTEGER)")
                db.execSQL("CREATE TABLE reading (id TEXT NOT NULL PRIMARY KEY,seen INTEGER NOT NULL,saved INTEGER NOT NULL)")
                // Previous check-ins record attendance, not continuous elapsed time. Never infer a start from gaps.
                listOf("check_ins","goals","blocked_domains","restricted_apps","protection_events","settings","milestones").forEach { db.execSQL("DROP TABLE IF EXISTS `$it`") }
            }
        }).build()
    }
}
