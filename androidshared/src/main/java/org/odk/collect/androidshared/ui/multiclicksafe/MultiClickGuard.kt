package org.odk.collect.androidshared.ui.multiclicksafe

import android.os.SystemClock

object MultiClickGuard {
    @JvmField
    var test = false

    private var lastClickTime: Long = 0
    private var lastClickName: String = javaClass.name

    // Debounce multiple clicks within the same screen
    @JvmStatic
    @JvmOverloads
    fun allowClick(className: String = javaClass.name, clickDebounceSeconds: Int = 1): Boolean {
        if (test) {
            return true
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val isSameClass = className == lastClickName
        val isBeyondThreshold = elapsedRealtime - lastClickTime > clickDebounceSeconds * 1000
        val isBeyondTestThreshold =
            lastClickTime == 0L || lastClickTime == elapsedRealtime // just for tests

        val allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold

        if (allowClick) {
            lastClickTime = elapsedRealtime
            lastClickName = className
        }
        return allowClick
    }
}
