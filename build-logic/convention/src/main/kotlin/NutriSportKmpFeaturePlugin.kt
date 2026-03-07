import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class NutriSportKmpFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "nutrisport.kmp.library")

        val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.findBundle("koin-compose").get())
                implementation(libs.findLibrary("messagebar-kmp").get())
            }
        }
    }
}
