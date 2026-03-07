import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class NutriSportKmpFeatureFullPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "nutrisport.kmp.feature")

        val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.findBundle("coil").get())
                implementation(libs.findLibrary("compose-navigation").get())
            }
            sourceSets.getByName("androidMain").dependencies {
                implementation(libs.findLibrary("ktor-android-client").get())
            }
            sourceSets.maybeCreate("iosMain").dependencies {
                implementation(libs.findLibrary("ktor-darwin-client").get())
            }
        }
    }
}
