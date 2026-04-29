pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AgendaGym"
include(":app")