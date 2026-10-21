plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.azhdar.app10"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.azhdar.app10"
        // camera2 torch (CameraManager#setTorchMode) requires API 23.
        minSdk = 23
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Ad zone ids are configured through local.properties (or -P flags) so a
        // misconfigured build disables ads instead of crashing at runtime.
        val tapsellAppKey = project.findProperty("TAPSELL_APP_KEY") as? String ?: ""
        val tapsellBannerZone = project.findProperty("TAPSELL_BANNER_ZONE") as? String ?: ""
        val tapsellInterstitialZone = project.findProperty("TAPSELL_INTERSTITIAL_ZONE") as? String ?: ""
        val tapsellRewardedZone = project.findProperty("TAPSELL_REWARDED_ZONE") as? String ?: ""

        buildConfigField("String", "TAPSELL_APP_KEY", "\"$tapsellAppKey\"")
        buildConfigField("String", "TAPSELL_BANNER_ZONE", "\"$tapsellBannerZone\"")
        buildConfigField("String", "TAPSELL_INTERSTITIAL_ZONE", "\"$tapsellInterstitialZone\"")
        buildConfigField("String", "TAPSELL_REWARDED_ZONE", "\"$tapsellRewardedZone\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    implementation("com.github.skydoves:colorpicker-compose:1.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("ir.tapsell.plus:tapsell-plus-sdk-android:2.2.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.6")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
