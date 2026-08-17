import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "zone.ien.inputactions"
version = libs.versions.lib.version.name.get()

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "zone.ien.inputactions"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "inputactions", version.toString())

    pom {
        name = "Compose Input Actions"
        description = "Native platform text-input actions and accessories for Compose Multiplatform TextFields."
        inceptionYear = "2026"
        url = "https://github.com/ienground/compose-input-actions"
        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "ienground"
                name = "IENLAB"
                url = "https://github.com/ienground"
            }
        }
        scm {
            url = "https://github.com/ienground/compose-input-actions.git"
            connection = "scm:git:https://github.com/ienground/compose-input-actions.git"
            developerConnection = "scm:git:https://github.com/ienground/compose-input-actions.git"
        }
    }
}
