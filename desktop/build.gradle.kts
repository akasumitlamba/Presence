import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}
kotlin { jvmToolchain(17) }
val generateAndroidUi by tasks.registering(Exec::class) {
    inputs.dir(rootProject.file("app/src/main/java/app/presence/ui"))
    inputs.file(rootProject.file("app/src/main/java/app/presence/data/Repo.kt"))
    inputs.file(rootProject.file("app/src/main/java/app/presence/timer/Center.kt"))
    inputs.file("tools/adapt_android_ui.py")
    outputs.dir(layout.buildDirectory.dir("generated/androidUi"))
    commandLine(if (System.getProperty("os.name").startsWith("Windows")) "python" else "python3", project.file("tools/adapt_android_ui.py"))
}
kotlin.sourceSets.main { kotlin.srcDir(layout.buildDirectory.dir("generated/androidUi")) }
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach { dependsOn(generateAndroidUi) }
tasks.processResources {
    from(rootProject.file("app/src/main/res/font")) { include("*.ttf"); into("font") }
    from(rootProject.file("assets/presence.png"))
    from(rootProject.file("third_party/source-sans-3")) { into("licenses/source-sans-3") }
}
dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("com.tianscar.javasound:javasound-aac:0.9.8")
    implementation("com.tianscar.javasound:javasound-flac:1.4.1")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
    implementation("org.json:json:20240303")
    implementation("org.jmdns:jmdns:3.6.1")
    implementation("org.slf4j:slf4j-nop:2.0.16")
    testImplementation("junit:junit:4.13.2")
}
compose.desktop {
    application {
        mainClass = "app.presence.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Presence"
            packageVersion = "2.2.1"
            description = "Offline focus timer and time tracker"
            vendor = "Sumit Kumar"
            copyright = "Copyright 2026 Sumit Kumar"
            licenseFile.set(rootProject.file("LICENSE"))
            modules("java.net.http", "java.prefs", "jdk.unsupported", "java.desktop", "java.logging")
            linux {
                packageName = "presence"
                debMaintainer = "akasumitlamba@users.noreply.github.com"
                appCategory = "Utility"
                menuGroup = "Office"
                iconFile.set(rootProject.file("assets/presence.png"))
            }
            windows {
                menuGroup = "Presence"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "731ba5f7-a92c-4784-bc83-49f572e9d237"
            }
        }
    }
}
