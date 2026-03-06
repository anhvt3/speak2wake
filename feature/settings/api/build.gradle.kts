plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.kotlin.serialization)
}
android { namespace = "com.speak2wake.feature.settings.api" }
dependencies {
    implementation(libs.kotlinx.serialization.json)
}
