package com.seacliff.pos.service

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import timber.log.Timber

object AppLifecycleObserver : DefaultLifecycleObserver {

    var isInForeground: Boolean = false
        private set

    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isInForeground = true
        Timber.d("App moved to foreground")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isInForeground = false
        Timber.d("App moved to background")
    }
}
