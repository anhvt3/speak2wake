import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            pluginManager.apply {
                apply("speak2wake.android.library")
                apply("speak2wake.android.library.compose")
                apply("speak2wake.hilt")
            }
            dependencies {
                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
                add(
                        "implementation",
                        libs.findLibrary("androidx.lifecycle.viewmodel.compose").get()
                )
                add("implementation", libs.findLibrary("androidx.navigation.compose").get())
            }
        }
    }
}
