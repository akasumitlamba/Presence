pluginManagement {
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

rootProject.name = "presence"
include(":core")
// Desktop builds do not require an Android SDK.
if (!providers.gradleProperty("desktopOnly").isPresent) include(":app")
include(":desktop")
