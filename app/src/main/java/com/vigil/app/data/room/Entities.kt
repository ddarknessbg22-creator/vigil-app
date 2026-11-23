package com.vigil.app.data.room

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable @Entity(tableName="journal")
data class Entry(@PrimaryKey val id:String, val day:String, val note:String, val mood:Int=3, val urge:Boolean=false, val setback:Boolean=false)
@Serializable @Entity(tableName="intentions")
data class Goal(@PrimaryKey val id:String, val title:String, val description:String="", val target:Int=1, val complete:Boolean=false)
@Serializable @Entity(tableName="runs")
data class Run(@PrimaryKey val start:Long, val end:Long?=null)
@Serializable @Entity(tableName="reading")
data class Reading(@PrimaryKey val id:String, val seen:Long, val saved:Boolean=false)
