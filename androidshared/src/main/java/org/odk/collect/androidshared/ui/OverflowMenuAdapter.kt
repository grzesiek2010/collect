package org.odk.collect.androidshared.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.androidshared.R

class OverflowMenuAdapter(
        context: Context,
        private val items: List<OverflowMenuItem>
) : ArrayAdapter<OverflowMenuAdapter.OverflowMenuItem>(
        context,
        R.layout.overflow_menu_item_layout,
        items
) {
    private inner class ViewHolder(view: View) {
        var icon: ImageView
        var title: MaterialTextView
        var toggle: SwitchMaterial

        init {
            icon = view.findViewById(R.id.icon)
            title = view.findViewById(R.id.title)
            toggle = view.findViewById(R.id.toggle)
        }

        fun onBind(item: OverflowMenuItem) {
            icon.setImageResource(item.icon)
            title.text = item.title
            toggle.visibility = if (item.isCheckable) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.overflow_menu_item_layout, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }
        holder.onBind(items[position])
        return view
    }

    fun measureContentWidth(): Int {
        val mMeasureParent = FrameLayout(context)
        var maxWidth = 0
        var itemView: View? = null
        var itemType = 0
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        for (i in 0 until count) {
            val positionType = getItemViewType(i)
            if (positionType != itemType) {
                itemType = positionType
                itemView = null
            }

            itemView = getView(i, itemView, mMeasureParent)
            itemView.measure(widthMeasureSpec, heightMeasureSpec)
            val itemWidth = itemView.measuredWidth
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth
            }
        }

        return maxWidth
    }

    class OverflowMenuItem(val icon: Int, val title: String, val isCheckable: Boolean)
}
