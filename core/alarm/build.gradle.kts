plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.speak2wake.hilt)
}

android { namespace = "com.speak2wake.core.alarm" }

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
