package org.odk.collect.externalapp

import android.app.Activity
import android.content.Intent
import android.net.Uri

/**
 * Helpers for returning answers to [FormEntryActivity] via [Activity.onActivityResult] and also
 * for dealing with those answers in [Activity.onActivityResult] itself.
 */
object ExternalAppUtils {

    private const val VALUE_KEY = "value"

    @JvmStatic
    fun returnSingleValue(activity: Activity, value: String) {
        activity.setResult(Activity.RESULT_OK, getReturnIntent(value))
        activity.finish()
    }

    @JvmStatic
    fun returnData(activity: Activity, value: Uri) {
        activity.setResult(Activity.RESULT_OK, getReturnIntentWithData(value))
        activity.finish()
    }

    @JvmStatic
    fun getReturnIntent(value: String): Intent {
        return Intent().also {
            it.putExtra(VALUE_KEY, value)
        }
    }

    @JvmStatic
    fun getReturnIntentWithData(value: Uri): Intent {
        return Intent().also {
            it.data = value
        }
    }

    @JvmStatic
    fun getReturnedSingleValue(data: Intent): Any? {
        return data.extras?.get(VALUE_KEY)
    }
}
