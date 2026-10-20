pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "ComposeInputActions"
include(":library")
include(":example")
include(":example:androidApp")
include(":example:composeApp")
include(":example:iosApp")
