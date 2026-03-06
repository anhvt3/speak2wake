plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.kotlin.serialization)
}
android { namespace = "com.speak2wake.feature.ring.api" }
dependencies {
    implementation(libs.kotlinx.serialization.json)
}
