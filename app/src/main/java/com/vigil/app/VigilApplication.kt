package com.vigil.app

import android.app.Application
import com.vigil.app.data.VigilRepository
import com.vigil.app.data.datastore.PreferencesDataStore
import com.vigil.app.data.room.VigilDatabase

/**
 * Simple manual service locator. No annotation processing, no reflection,
 * and nothing to fail at build time.
 */
class VigilApplication : Application() {

    lateinit var database: VigilDatabase
        private set

    lateinit var preferences: PreferencesDataStore
        private set

    lateinit var repository: VigilRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = VigilDatabase.getInstance(this)
        preferences = PreferencesDataStore(this)
        repository = VigilRepository(database, preferences)
    }

    companion object {
        fun from(context: android.content.Context): VigilApplication =
            context.applicationContext as VigilApplication
    }
}
