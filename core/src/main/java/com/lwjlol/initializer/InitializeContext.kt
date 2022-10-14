package com.lwjlol.initializer

import kotlinx.coroutines.CoroutineScope
import android.content.Context as AndroidCtx

/**
 * @author luwenjie on 2022/10/11 14:04:08
 */
class InitializeContext(val appContext: AndroidCtx, val coroutineScope: CoroutineScope) {

    companion object {
        private const val TAG = "Context"
    }
}