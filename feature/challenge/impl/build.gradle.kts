plugins {
    alias(libs.plugins.speak2wake.android.feature)
    alias(libs.plugins.speak2wake.android.library.compose)
    alias(libs.plugins.speak2wake.hilt)
}
android { namespace = "com.speak2wake.feature.challenge.impl" }
dependencies {
    api(projects.feature.challenge.api)
    implementation(projects.core.data)
    implementation(projects.core.alarm)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(libs.androidx.activity.compose)
}
