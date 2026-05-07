import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

kotlin { explicitApi() }

val navigineSdkVersionProvider = providers.gradleProperty("navigineSdk")
    .orElse(providers.environmentVariable("NAVIGINE_SDK_VERSION"))
    .orElse("2.24.5")

val navigineSdkVersion = navigineSdkVersionProvider.get()

val isPublishing = gradle.startParameter.taskRequests.any { req ->
    req.args.any { it.contains("publish", ignoreCase = true) }
}
if (isPublishing) {
    check(navigineSdkVersion.isNotBlank()) {
        "Set Navigine SDK version via -PnavigineSdk=x.yy.z or NAVIGINE_SDK_VERSION env or navigation/mobile/release/VERSION"
    }
}

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
//    api(libs.navigine.android.mobile.sdk)
//    api("com.navigine:navigine:0.0.1-local")

    //noinspection UseTomlInstead
    api("com.github.Navigine:Indoor-Navigation-Android-Mobile-SDK-2.0:${navigineSdkVersion}")

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.recyclerview)  // for default location view
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
    // ./gradlew :location-view:publishToMavenLocal
    coordinates(
        "com.navigine",
        "navigine-locationview-compose",
        navigineSdkVersion
    )

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )

    publishToMavenCentral(automaticRelease = true)
    val isCi = listOf("CI", "GITLAB_CI")
        .any { providers.environmentVariable(it).map { v -> v.equals("true", true) || v == "1" }.isPresent }
    if (isCi) {
        signAllPublications()
    }

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