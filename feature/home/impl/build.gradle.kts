plugins {
    alias(libs.plugins.speak2wake.android.feature)
    alias(libs.plugins.speak2wake.android.library.compose)
    alias(libs.plugins.speak2wake.hilt)
}
android { namespace = "com.speak2wake.feature.home.impl" }
dependencies {
    api(projects.feature.home.api)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
}
