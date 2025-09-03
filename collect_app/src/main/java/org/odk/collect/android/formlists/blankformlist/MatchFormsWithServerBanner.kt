package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.android.databinding.MatchFormsWithServerBannerBinding

class MatchFormsWithServerBanner@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding = MatchFormsWithServerBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun setData(lastMatchFormsWithServerCompletedTime: Long?, isSyncing: Boolean) {
        if (lastMatchFormsWithServerCompletedTime != null) {

        }
    }
}
