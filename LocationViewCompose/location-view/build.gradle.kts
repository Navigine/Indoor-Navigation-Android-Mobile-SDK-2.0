import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

kotlin { explicitApi() }

android {
    namespace = "com.navigine.location_view"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures { compose = true }
    lint { abortOnError = true }
}

dependencies {
    api(libs.navigine.android.mobile.sdk)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.foundation)
    api(libs.androidx.runtime)
    debugImplementation(libs.androidx.ui.tooling.preview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {

    coordinates(
        "com.navigine",
        "navigine-locationview-compose",
        "1.7.2"
    )

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    pom {
        name.set("Navigine LocationView Compose")
        description.set("Compose wrapper for Navigine LocationView")
        url.set("https://navigine.com")
        licenses {
            license {
                name.set("Proprietary")
                url.set("https://navigine.com/privacy/#end_user ")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("navigine")
                name.set("Denis Khamidullin")
                organization.set("Navigine")
                organizationUrl.set("https://navigine.com")
            }
        }
        scm {
            url.set("https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0")
            connection.set("scm:git:https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0.git")
            developerConnection.set("scm:git:ssh://git@github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0.git")
        }
        issueManagement {
            url.set("https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/issues")
        }
    }
}