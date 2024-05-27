package org.odk.collect.androidtest

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

object DrawableMatcher {
    @JvmStatic
    fun withImageDrawable(expectedResourceId: Int): Matcher<View> {
        return object : BoundedMatcher<View, ImageView>(ImageView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has image drawable resource $expectedResourceId")
            }

            public override fun matchesSafely(imageView: ImageView): Boolean {
                val context = ApplicationProvider.getApplicationContext<Application>()
                return imageView.drawable.toBitmap().sameAs((AppCompatResources.getDrawable(context, expectedResourceId) as VectorDrawable).toBitmap())
            }
        }
    }

    @JvmStatic
    fun withBitmap(match: Bitmap?): Matcher<View> {
        return object : BoundedMatcher<View, ImageView>(ImageView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("bitmaps did not match")
            }

            override fun matchesSafely(imageView: ImageView): Boolean {
                val drawable = imageView.drawable
                if (drawable == null && match == null) {
                    return true
                } else if (drawable != null && match == null) {
                    return false
                } else if (drawable == null) {
                    return false
                }

                val actual = (drawable as BitmapDrawable).bitmap

                val originalThreadPolicy = StrictMode.getThreadPolicy()

                try {
                    // Permit slow calls to allow `sameAs` use
                    StrictMode.setThreadPolicy(
                        ThreadPolicy.Builder()
                            .permitCustomSlowCalls().build()
                    )

                    return actual.sameAs(match)
                } finally {
                    StrictMode.setThreadPolicy(originalThreadPolicy)
                }
            }
        }
    }
}
