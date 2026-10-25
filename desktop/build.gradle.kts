import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
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
            modules("java.net.http", "java.prefs", "jdk.unsupported")
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
