package org.odk.collect.android.formentry

import android.content.Context
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.forms.Form
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import java.util.Locale

class FormEndViewFactory(
    private val settingsProvider: SettingsProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider
) {
    fun createFormEndView(
        context: Context,
        formName: String,
        form: Form,
        listener: FormEndView.Listener
    ): FormEndView {
        val isSaveAsDraftEnabled = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT)
        val isFinalizeEnabled = settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_FINALIZE)
        val shouldFormBeSentAutomatically = if (autoSendSettingsProvider.isAutoSendEnabledInSettings()) {
            form.autoSend == null || form.autoSend.trim().lowercase(Locale.US) != "false"
        } else {
            form.autoSend != null && form.autoSend.trim().lowercase(Locale.US) == "true"
        }

        return FormEndView(
            context,
            formName,
            isSaveAsDraftEnabled,
            isFinalizeEnabled,
            shouldFormBeSentAutomatically,
            listener
        )
    }
}
