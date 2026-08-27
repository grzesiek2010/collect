package org.odk.collect.settings.validation

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class OriginalJsonSchemaSettingsValidatorTest {

    /*
     * Some settings end up replaced by new settings in but we need the schema to still
     * recognize the old fields so that we can migrate them correctly.
     */
    @Test
    fun `isValueSupported returns true for fields we no longer use`() {
        val validator = JsonSchemaSettingsValidator {
            javaClass.getResourceAsStream("/client-settings.schema.json")!!
        }

        removedKeys.forEach {
            assertThat(
                validator.isValueSupported(it.first, it.second, "true"),
                equalTo(true)
            )
        }
    }

    /*
     * Some setting values end up replaced by new values but we need the schema to still
     * recognize the old values so that we can migrate them correctly.
     */
    @Test
    fun `isValueSupported returns true for values we no longer use`() {
        val validator = JsonSchemaSettingsValidator {
            javaClass.getResourceAsStream("/client-settings.schema.json")!!
        }

        removedValues.forEach {
            assertThat(
                validator.isValueSupported(it.first, it.second, it.third),
                equalTo(true)
            )
        }
    }

    private val removedKeys = listOf(
        Pair("admin", "mark_as_finalized"),
        Pair("general", "default_completed"),
        Pair("admin", "finalize")
    )

    private val removedValues = listOf(
        Triple("general", "basemap_source", "mapbox")
    )
}
