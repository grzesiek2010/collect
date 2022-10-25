package org.odk.collect.hilttest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {

    @Inject
    lateinit var hiltTestNameProvider: HiltTestNameProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt_test)

        findViewById<TextView>(R.id.hilt_text_view).text = hiltTestNameProvider.getName()
    }
}
