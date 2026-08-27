plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.maplibre"
}

dependencies {
    implementation(project(":androidshared"))
    implementation(project(":maps"))
    implementation(project(":settings"))
    implementation(project(":shared"))
    implementation(project(":strings"))
    implementation(libs.androidxFragmentKtx)
    implementation(libs.androidxPreferenceKtx)
    implementation(libs.timber)
    implementation(libs.maplibreAndroidSdk)
    implementation(libs.maplibreAnnotationPlugin) {
        // The plugin brings its own older copy of the SDK, which clashes with the classes and
        // native libraries of the variant we depend on above
        exclude(group = "org.maplibre.gl", module = "android-sdk")
    }
    implementation(libs.maplibreScalebarPlugin) {
        exclude(group = "org.maplibre.gl", module = "android-sdk")
    }
}
