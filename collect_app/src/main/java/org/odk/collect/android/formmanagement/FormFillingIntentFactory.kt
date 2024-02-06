package org.odk.collect.android.formmanagement

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.odk.collect.android.external.FormUriActivity
import org.odk.collect.android.external.InstancesContract

object FormFillingIntentFactory {
    fun newInstanceIntent(
        context: Context,
        uri: Uri?
    ): Intent {
        return Intent(context, FormUriActivity::class.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = uri
        }
    }

    @JvmStatic
    fun editInstanceIntent(
        context: Context,
        uri: Uri?
    ): Intent {
        return Intent(context, FormUriActivity::class.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = uri
        }
    }

    @JvmStatic
    fun editInstanceIntent(
        context: Context,
        projectId: String,
        instanceId: Long
    ): Intent {
        return Intent(context, FormUriActivity::class.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = InstancesContract.getUri(projectId, instanceId)
        }
    }
}
