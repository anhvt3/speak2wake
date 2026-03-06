plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.kotlin.serialization)
}
android { namespace = "com.speak2wake.feature.home.api" }
dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
