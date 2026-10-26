import java.security.KeyStore
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "app.presence"
    compileSdkVersion("android-37.0")

    defaultConfig {
        applicationId = "app.presence"
        minSdk = 26
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 25
        versionName = "1.1.1"
    }

    signingConfigs {
        create("presence") {
            // Keep the verified update key outside Git; never generate a replacement for releases.
            storeFile = file(providers.environmentVariable("PRESENCE_KEYSTORE").orElse("${System.getProperty("user.home")}/.android/debug.keystore").get())
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("presence")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    lint {
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    implementation(project(":core"))
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.5.0-beta02")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}

// A different debug key would break updates for existing installations.
val verifyPresenceSigning by tasks.registering {
    doLast {
        val config = android.signingConfigs.getByName("presence")
        val keys = KeyStore.getInstance(config.storeType ?: "JKS")
        config.storeFile!!.inputStream().use { keys.load(it, config.storePassword!!.toCharArray()) }
        val certificate = keys.getCertificate(config.keyAlias)
        val fingerprint = MessageDigest.getInstance("SHA-256").digest(certificate.encoded)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        check(fingerprint == "cdd362791c6f2f1c64c9803ad1859631f9c4acd99d3a8f2b4a6d0c617db92b12") {
            "Presence release signing certificate differs from the verified update key. Set PRESENCE_KEYSTORE to the existing keystore."
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(verifyPresenceSigning) }
