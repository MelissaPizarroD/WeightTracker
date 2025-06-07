plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.isoft.weighttracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.isoft.weighttracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
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
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui-text:1.6.0-beta03")
    implementation("androidx.compose.ui:ui:1.6.0-beta03")

    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    //para notificaciones
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    //para el Async
    implementation("io.coil-kt:coil-compose:2.7.0")
    //Para el ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2") // Si estas usando Firebase/Google

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    //Firebase GoogleCloud
    implementation("com.google.firebase:firebase-firestore-ktx")

    //Google
        implementation("com.google.android.gms:play-services-auth:21.3.0")
        implementation("androidx.credentials:credentials:1.5.0")
        implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
        implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
        implementation ("com.google.code.gson:gson:2.13.1")

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.16.0")
}