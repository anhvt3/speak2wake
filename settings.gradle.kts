pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Speak2Wake"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core
include(":core:model")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:alarm")
include(":core:common")

// Features
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:create:api")
include(":feature:create:impl")
include(":feature:ring:api")
include(":feature:ring:impl")
include(":feature:challenge:api")
include(":feature:challenge:impl")
include(":feature:settings:api")
include(":feature:settings:impl")
