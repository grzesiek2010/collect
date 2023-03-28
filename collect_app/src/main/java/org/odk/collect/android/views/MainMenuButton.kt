package org.odk.collect.android.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class MainMenuButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.main_menu_button, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ElevatedIconButton,
            0, 0).apply {

            try {
                val buttonIcon = this.getResourceId(R.styleable.ElevatedIconButton_button_icon, 0)
                val buttonName = this.getString(R.styleable.ElevatedIconButton_button_name)

                findViewById<ImageView>(R.id.icon).setImageResource(buttonIcon)
                findViewById<TextView>(R.id.text).text = buttonName
            } finally {
                recycle()
            }
        }
    }

    override fun performClick(): Boolean {
        return MultiClickGuard.allowClick() && super.performClick()
    }

    fun setNumber(number: Int) {
        if (number < 1) {
            findViewById<TextView>(R.id.number).text = ""
        } else {
            findViewById<TextView>(R.id.number).text = number.toString()
        }
    }
}
