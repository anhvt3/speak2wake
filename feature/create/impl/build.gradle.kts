plugins {
    alias(libs.plugins.speak2wake.android.feature)
    alias(libs.plugins.speak2wake.android.library.compose)
    alias(libs.plugins.speak2wake.hilt)
}
android { namespace = "com.speak2wake.feature.create.impl" }
dependencies {
    api(projects.feature.create.api)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
}
