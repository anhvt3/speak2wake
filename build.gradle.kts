plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("speak2wake.android.application") version "unspecified" apply false
    id("speak2wake.android.library") version "unspecified" apply false
    id("speak2wake.android.feature") version "unspecified" apply false
    id("speak2wake.android.library.compose") version "unspecified" apply false
    id("speak2wake.hilt") version "unspecified" apply false
}

subprojects {
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            jvmToolchain(17)
        }
    }
}
