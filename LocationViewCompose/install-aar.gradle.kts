plugins {
    id("maven-publish")
}

val aarFile = file("location-view/libs/navigine-release06112025.aar")
if (!aarFile.exists()) {
    throw GradleException("Не найден AAR: ${aarFile.absolutePath}")
}

group = "com.navigine"
version = "0.0.1-local"


// ./gradlew -b install-aar.gradle.kts publishAarToLocal --info
publishing {
    publications {
        create<MavenPublication>("navigineLocal") {
            groupId = "com.navigine"
            artifactId = "navigine"
            version = "0.0.1-local"

            artifact(aarFile) {
                extension = "aar"
            }

            pom {
                name.set("Navigine Local")
                description.set("Local AAR for testing")
                packaging = "aar"
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.register("publishAarToLocal") {
    dependsOn("publishNavigineLocalPublicationToMavenLocal")
}
