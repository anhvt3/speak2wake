plugins {
    alias(libs.plugins.speak2wake.android.feature)
    alias(libs.plugins.speak2wake.android.library.compose)
    alias(libs.plugins.speak2wake.hilt)
}
android { namespace = "com.speak2wake.feature.settings.impl" }
dependencies {
    api(projects.feature.settings.api)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(libs.androidx.datastore)
}
