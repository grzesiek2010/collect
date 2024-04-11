package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PolygonDescriptionTest {
    @Test
    fun `getFillColor returns the default color when the passed one is null`() {
        val polygonDescription = PolygonDescription(emptyList(), null)
        assertThat(polygonDescription.getFillColor(), equalTo(1157562368))
    }

    @Test
    fun `getFillColor returns the default color when the passed one is invalid`() {
        val polygonDescription = PolygonDescription(emptyList(), "blah")
        assertThat(polygonDescription.getFillColor(), equalTo(1157562368))
    }

    @Test
    fun `getFillColor returns custom color when it is valid`() {
        val polygonDescription = PolygonDescription(emptyList(), "#aaccee")
        assertThat(polygonDescription.getFillColor(), equalTo(1152044270))
    }
}