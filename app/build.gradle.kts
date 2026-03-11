import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.speak2wake.android.application)
    alias(libs.plugins.speak2wake.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.speak2wake"
    defaultConfig {
        applicationId = "com.speak2wake"
        versionCode = 9
        versionName = "2.8"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }
    val keystorePropertiesFile = rootProject.file("local.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties["storeFile"]?.let { file(it) } ?: file("speak2wake-release.keystore")
            storePassword = keystoreProperties["storePassword"] as String? ?: ""
            keyAlias = keystoreProperties["keyAlias"] as String? ?: ""
            keyPassword = keystoreProperties["keyPassword"] as String? ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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
    
    implementation(libs.play.services.ads)
}
