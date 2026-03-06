plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.speak2wake.android.library.compose)
}

android {
    namespace = "com.speak2wake.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.material.icons.extended)
}
