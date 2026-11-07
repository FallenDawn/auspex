pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "2.3.10"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // GitHub Packages for UVCCamera fork (cptskippy/UVCCamera)
        maven {
            url = uri("https://maven.pkg.github.com/cptskippy/UVCCamera")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR").orEmpty()
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN").orEmpty()
            }
        }
    }
}

rootProject.name = "Auspex"
include(":app")
