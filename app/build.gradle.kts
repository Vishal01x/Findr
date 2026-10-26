plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.exa.android.reflekt"
    compileSdk = 37
    // Add this line to enable legacy Apache HTTP support
    //useLibrary("org.apache.http.legacy")

    defaultConfig {
        applicationId = "com.exa.android.reflekt"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "2.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
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
            excludes += "META-INF/INDEX.LIST"
        }
    }
    splits {
        abi {
            isEnable = false
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

}

dependencies {

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.browser)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.dynamic.links)
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

    //datastore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    //for permission dialog
    implementation(libs.accompanist.permissions)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)


    // hilt Navigation
    implementation(libs.androidx.hilt.navigation.compose)

    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // HTML parsing
    implementation(libs.jsoup)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
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


    implementation(libs.places)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.timber)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

//    // fm
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Logging for debugging
    implementation(libs.google.auth.library.oauth2.http) {  // Google OAuth2
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }

    // new dependency
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.compose.markdown)

    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // security crypto
    implementation(libs.androidx.security.crypto)

    implementation(libs.androidx.core.splashscreen)

    // Lottie animations for onboarding and auth screens
    implementation(libs.lottie.compose)

    // Google Sign-In for authentication
    implementation(libs.play.services.auth)

    // internal libraries
    implementation(project(":library_ui"))
}