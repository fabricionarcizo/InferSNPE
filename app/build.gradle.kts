plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gn.videotech.infersnpe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gn.videotech.infersnpe"
        minSdk = 26
        targetSdk = 30  // You must set the target SDK to API 30 to enable GPU and DSP modes.
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters.add("arm64-v8a")  // Compile the APK only for ARM64 devices.
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs.useLegacyPackaging = true  // Enable DSP support.
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(files("src/main/libs/snpe-release.aar"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
}