package org.odk.collect.shared.locks

import java.util.function.Function

class ThreadSafeBooleanChangeLock : ChangeLock {
    private var locked = false

    override fun <T> withLock(function: Function<Boolean, T>): T {
        val acquired = tryLock()

        return try {
            function.apply(acquired)
        } finally {
            unlock()
        }
    }

    override fun tryLock(): Boolean {
        return synchronized(this) {
            if (locked) {
                false
            } else {
                locked = true
                true
            }
        }
    }

    override fun unlock() {
        synchronized(this) {
            locked = false
        }
    }
}