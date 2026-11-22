package com.vigil.app

import android.content.Context
import com.vigil.app.blocking.BlocklistManager
import com.vigil.app.blocking.root.RootManager

/**
 * Tiny service accessor so services, workers and composables share one
 * instance graph without an annotation processor.
 */
object AppServices {

    @Volatile private var blocklistRef: BlocklistManager? = null
    @Volatile private var rootRef: RootManager? = null

    fun app(context: Context): VigilApplication =
        context.applicationContext as VigilApplication

    fun blocklist(context: Context): BlocklistManager =
        blocklistRef ?: synchronized(this) {
            blocklistRef ?: BlocklistManager(app(context), app(context).repository)
                .also { blocklistRef = it }
        }

    fun root(context: Context): RootManager =
        rootRef ?: synchronized(this) {
            rootRef ?: RootManager(context.applicationContext).also { rootRef = it }
        }
}
