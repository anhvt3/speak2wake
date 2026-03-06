plugins {
    alias(libs.plugins.speak2wake.android.application)
    alias(libs.plugins.speak2wake.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.speak2wake"
    defaultConfig {
        applicationId = "com.speak2wake"
        versionCode = 5
        versionName = "2.4"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "speak2wake-v${variant.versionName}.apk"
        }
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.alarm)
    implementation(projects.core.designsystem)
    implementation(projects.core.common)

    implementation(projects.feature.home.impl)
    implementation(projects.feature.create.impl)
    implementation(projects.feature.ring.impl)
    implementation(projects.feature.challenge.impl)
    implementation(projects.feature.settings.impl)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
