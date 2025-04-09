package org.odk.collect.formstest

import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.TempFiles.createTempFile
import org.odk.collect.shared.strings.RandomString
import java.io.File
import kotlin.random.Random

object InstanceUtils {

    @JvmStatic
    fun buildInstance(formId: String?, version: String?, instancesDir: String): Instance.Builder {
        return buildInstance(
            formId,
            version,
            "display name",
            Instance.STATUS_INCOMPLETE,
            null,
            instancesDir
        )
    }

    @JvmStatic
    fun buildInstance(
        formId: String?,
        version: String?,
        displayName: String?,
        status: String?,
        deletedDate: Long?,
        instancesDir: String
    ): Instance.Builder {
        val instanceFile = createInstanceDirAndFile(instancesDir)

        return Instance.Builder()
            .formId(formId)
            .formVersion(version)
            .displayName(displayName)
            .instanceFilePath(PathUtils.getRelativeFilePath(instancesDir, instanceFile.absolutePath))
            .status(status)
            .deletedDate(deletedDate)
    }

    @JvmStatic
    fun createInstanceDirAndFile(instancesDir: String): File {
        val instanceDir = File(instancesDir + File.separator + RandomString.randomString(5) + "_" + Random.nextLong())
        instanceDir.mkdir()

        return createTempFile(instanceDir, instanceDir.name, ".xml").also {
            it.writeText(RandomString.randomString(10))
        }
    }
}
