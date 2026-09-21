pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "GitLabNavigineTrackingSdk"
            url = uri("https://gitlab.navigine.com/api/v4/projects/86/packages/maven")
            credentials {
                username = providers.gradleProperty("gitlabNavigineDeployTokenUsername")
                    .orElse(providers.environmentVariable("GITLAB_NAVIGINE_DEPLOY_TOKEN_USERNAME")).get()
                password = providers.gradleProperty("gitlabNavigineDeployTokenPassword")
                    .orElse(providers.environmentVariable("GITLAB_NAVIGINE_DEPLOY_TOKEN_PASSWORD")).get()
            }
        }
    }
}

rootProject.name = "LocationViewCompose"
include(":app")
include(":location-view")
