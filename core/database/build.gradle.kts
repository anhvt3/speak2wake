plugins {
    alias(libs.plugins.speak2wake.android.library)
    alias(libs.plugins.speak2wake.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android { namespace = "com.speak2wake.core.database" }

room { schemaDirectory("$projectDir/schemas") }

dependencies {
    implementation(projects.core.model)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
