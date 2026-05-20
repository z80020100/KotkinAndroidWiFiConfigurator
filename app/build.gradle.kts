plugins {
    alias(libs.plugins.android.application)
}

// Signing config is activated only when all four env vars are present.
// Without them, assembleRelease still produces an unsigned APK (e.g. for
// local smoke tests).
val releaseStoreFile: String? = System.getenv("RELEASE_STORE_FILE")
val releaseKeyAlias: String? = System.getenv("RELEASE_KEY_ALIAS")
val releaseStorePassword: String? = System.getenv("RELEASE_STORE_PASSWORD")
val releaseKeyPassword: String? = System.getenv("RELEASE_KEY_PASSWORD")
val hasReleaseSigning =
    !releaseStoreFile.isNullOrEmpty() &&
        !releaseKeyAlias.isNullOrEmpty() &&
        !releaseStorePassword.isNullOrEmpty() &&
        !releaseKeyPassword.isNullOrEmpty()

// versionCode is derived from versionName as MAJOR*10000 + MINOR*100 + PATCH.
// Each segment must stay in 0..99.
val appVersionName = "1.1.0"
val (major, minor, patch) = appVersionName.split(".").map(String::toInt)
val appVersionCode = major * 10000 + minor * 100 + patch

android {
    namespace = "com.example.wi_ficonfigurator"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.wi_ficonfigurator"
        minSdk = 30
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                keyAlias = releaseKeyAlias
                storePassword = releaseStorePassword
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}