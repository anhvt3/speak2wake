plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.speak2wake.hilt)
}

android {
    namespace = "com.speak2wake.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.alarm)
    implementation(libs.kotlinx.coroutines.android)
    api(libs.androidx.datastore)
}
