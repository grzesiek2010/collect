import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import java.util.Properties

object Secrets {
    @TaskAction
    fun getSecrets(rootDir: File): Properties {
        val secretsFile = File(rootDir, "secrets.properties")
        val oldSecretsFile = File(rootDir, "collect_app/secrets.properties")

        if (!secretsFile.exists() && oldSecretsFile.exists()) {
            throw InvalidUserDataException("Please move secrets.properties to the root directory!")
        }

        val secrets = Properties()
        if (secretsFile.exists()) {
            secrets.load(FileInputStream(secretsFile))
        }

        return secrets
    }
}
