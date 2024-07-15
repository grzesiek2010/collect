package org.odk.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

open class MultiClickSafeMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    override fun performClick(): Boolean {
        return allowClick() && super.performClick()
    }
}
