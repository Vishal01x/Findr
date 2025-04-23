plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
}

android {
    namespace = "com.exa.android.reflekt"
    compileSdk = 35
    // Add this line to enable legacy Apache HTTP support
    //useLibrary("org.apache.http.legacy")

    defaultConfig {
        applicationId = "com.exa.android.reflekt"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
//            isMinifyEnabled = true // <--- Enable shrinking
//            isShrinkResources = true // Optional: Remove unused resources
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.compose.foundation:foundation:1.7.8")
    implementation ("androidx.compose.material3:material3:1.3.2")

    implementation ("androidx.compose.material:material:1.7.8")

    // system ui
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // Stream Video SDK
    implementation(libs.stream.video.ui.compose)
    implementation(libs.stream.video.ui.previewdata)

    //for permission dialog
    implementation(libs.accompanist.permissions)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // hilt Navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //coil
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-svg:2.4.0")

    // HTML parsing
    implementation("org.jsoup:jsoup:1.17.2")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("com.google.code.gson:gson:2.10.1")

    // map
    implementation("com.google.maps.android:maps-compose:6.4.1")
    implementation("com.google.android.gms:play-services-maps:19.1.0")

    // geofire
    implementation("com.firebase:geofire-android:3.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // image
    implementation(libs.glide)
    implementation(libs.landscapist.glide)
    kapt("com.github.bumptech.glide:compiler:4.16.0")


    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.google.maps.android:places-ktx:3.1.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // fm
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Logging for debugging
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") // Google OAuth2

    // new dependency
    implementation("androidx.compose.material:material-icons-extended:1.7.0")


    // All:
    implementation("com.cloudinary:cloudinary-android:3.0.2")

// Download + Preprocess:
    implementation("com.cloudinary:cloudinary-android-download:3.0.2")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.0.2")

    //work Manager
    // Kotlin + coroutines
    val work_version = "2.10.0"
    implementation( "androidx.work:work-runtime-ktx:$work_version")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // security crypto
    implementation ("androidx.security:security-crypto:1.1.0-alpha07")
//
//    implementation ("androidx.media3:media3-exoplayer:1.6.1")
//    implementation ("androidx.media3:media3-ui:1.6.1")
//    implementation ("androidx.media3:media3-common:1.6.1")

    implementation("androidx.core:core-splashscreen:1.0.1")
//
//    // Lottie animations
//    implementation ("com.airbnb.android:lottie-compose:6.1.0")
//
//    // Motion Layout
//    implementation ("androidx.constraintlayout:constraintlayout:2.2.1")

}