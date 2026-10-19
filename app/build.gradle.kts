import com.android.build.api.artifact.SingleArtifact

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.murmel"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.murmel"
        // Android 12+: required for the on-device speech recognizer.
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    // Compile with JDK 17; Gradle downloads it automatically if it isn't installed.
    jvmToolchain(17)
}

// Print failed tests with message and stack trace in the terminal, so the
// "File.kt:42" lines are clickable in the IDE instead of only in the HTML report.
tasks.withType<Test>().configureEach {
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// CLAUDE.md rule 1: no INTERNET permission. Libraries can add permissions through
// manifest merging, so check the final merged manifest of each release build, not
// only our own AndroidManifest.xml. Runs with `./gradlew check` and in CI.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val noInternetCheck = tasks.register<NoInternetPermissionCheck>(
            "check${variant.name.replaceFirstChar { it.uppercase() }}NoInternet",
        ) {
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            variantName.set(variant.name)
        }
        tasks.named("check") { dependsOn(noInternetCheck) }
    }
}

abstract class NoInternetPermissionCheck : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mergedManifest: RegularFileProperty

    @get:Input
    abstract val variantName: Property<String>

    @TaskAction
    fun verify() {
        val manifest = mergedManifest.get().asFile
        if ("android.permission.INTERNET" in manifest.readText()) {
            throw GradleException(
                "The merged ${variantName.get()} manifest requests android.permission.INTERNET " +
                    "(CLAUDE.md rule 1: nothing leaves the phone).\n" +
                    "Merged manifest: ${manifest.path}\n" +
                    "To see which library adds it, search for INTERNET in " +
                    "app/build/outputs/logs/manifest-merger-${variantName.get()}-report.txt",
            )
        }
    }
}
