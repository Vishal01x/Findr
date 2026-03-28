plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.exa.android.reflekt"
    compileSdk = 36
    // Add this line to enable legacy Apache HTTP support
    //useLibrary("org.apache.http.legacy")

    defaultConfig {
        applicationId = "com.exa.android.reflekt"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.safetynet)
    implementation(libs.androidx.browser)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.material)

    // system ui
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    //datastore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    //for permission dialog
    implementation(libs.accompanist.permissions)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)


    // hilt Navigation
    implementation(libs.androidx.hilt.navigation.compose)

    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // HTML parsing
    implementation(libs.jsoup)

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.gson)

    // map
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    // geofire
    implementation(libs.geofire.android)
    implementation(libs.play.services.location)

    // image
    implementation(libs.glide)
    implementation(libs.landscapist.glide)
    kapt("com.github.bumptech.glide:compiler:5.0.5")


    implementation(libs.places)
    implementation(libs.places.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.timber)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

//    // fm
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Logging for debugging
    implementation(libs.google.auth.library.oauth2.http) // Google OAuth2

    // new dependency
    implementation(libs.androidx.compose.material.icons.extended)

    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // security crypto
    implementation(libs.androidx.security.crypto)

    implementation(libs.androidx.core.splashscreen)

    // Lottie animations for onboarding and auth screens
    implementation(libs.lottie.compose)

    // Google Sign-In for authentication
    implementation(libs.play.services.auth)

}