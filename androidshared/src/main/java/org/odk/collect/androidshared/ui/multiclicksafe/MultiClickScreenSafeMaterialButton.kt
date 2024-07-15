package org.odk.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.google.android.material.button.MaterialButton
import org.odk.collect.androidshared.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

/**
 * Prevents double clicks within the same screen. A double click on any button on a screen
 * prevents another click on any other button on the same screen within a short time frame,
 * but allows clicks on buttons in different screens.
 */
class MultiClickScreenSafeMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    private lateinit var screenName: String

    init {
        context.withStyledAttributes(attrs, R.styleable.MultiClickScreenSafeMaterialButton) {
            screenName = getString(R.styleable.MultiClickScreenSafeMaterialButton_screenName)
                ?: throw IllegalArgumentException("The screenName attribute is required and must be specified.")
        }
    }

    override fun performClick(): Boolean {
        return allowClick(screenName) && super.performClick()
    }
}
