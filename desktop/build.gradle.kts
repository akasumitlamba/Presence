import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO
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
val desktopIcons = layout.buildDirectory.dir("generated/desktopIcons")
val generateDesktopIcons by tasks.registering {
    inputs.file(rootProject.file("assets/presence.png"))
    outputs.dir(desktopIcons)
    doLast {
        val source = ImageIO.read(rootProject.file("assets/presence.png"))
        val directory = desktopIcons.get().asFile.apply { mkdirs() }
        fun rounded(size: Int): BufferedImage {
            val image = BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.drawImage(source,0,0,size,size,null)
            val mask = BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB)
            mask.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON)
                color = Color.WHITE
                fill(RoundRectangle2D.Float(0f,0f,size.toFloat(),size.toFloat(),size*0.42f,size*0.42f))
                dispose()
            }
            g.composite = AlphaComposite.DstIn
            g.drawImage(mask,0,0,null)
            g.dispose()
            return image
        }
        ImageIO.write(rounded(256),"png",directory.resolve("presence.png"))
        ImageIO.write(rounded(128),"png",directory.resolve("presence-128.png"))
        ImageIO.write(rounded(64),"png",directory.resolve("presence-64.png"))
        val sizes = listOf(16,24,32,48,64,128,256)
        val images = sizes.map { size -> ByteArrayOutputStream().also { ImageIO.write(rounded(size),"png",it) }.toByteArray() }
        val headerSize = 6+16*sizes.size
        val ico = ByteBuffer.allocate(headerSize+images.sumOf { it.size }).order(ByteOrder.LITTLE_ENDIAN)
        ico.putShort(0); ico.putShort(1); ico.putShort(sizes.size.toShort())
        var offset = headerSize
        sizes.forEachIndexed { index,size ->
            ico.put((if(size == 256) 0 else size).toByte()); ico.put((if(size == 256) 0 else size).toByte())
            ico.put(0); ico.put(0); ico.putShort(1); ico.putShort(32); ico.putInt(images[index].size); ico.putInt(offset)
            offset += images[index].size
        }
        images.forEach { ico.put(it) }
        directory.resolve("presence.ico").writeBytes(ico.array())
    }
}
tasks.matching { it.name.startsWith("package") || it.name == "createDistributable" || it.name == "prepareAppResources" }.configureEach { dependsOn(generateDesktopIcons) }
tasks.processResources {
    dependsOn(generateDesktopIcons)
    from(rootProject.file("app/src/main/res/font")) { include("*.ttf"); into("font") }
    from(desktopIcons) { include("presence.png") }
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
    implementation("com.github.trilarion:java-vorbis-support:1.2.1")
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
                iconFile.set(desktopIcons.map { it.file("presence.png") })
            }
            windows {
                iconFile.set(desktopIcons.map { it.file("presence.ico") })
                menuGroup = "Presence"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "731ba5f7-a92c-4784-bc83-49f572e9d237"
            }
        }
    }
}
