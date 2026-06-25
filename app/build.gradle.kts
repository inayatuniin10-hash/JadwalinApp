plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.jadwalin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jadwalin"
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
}

dependencies {
    // UI & Core Libraries
    implementation(libs.appcompat)
    implementation("androidx.core:core-ktx:1.15.0") // Menggunakan versi stabil untuk SDK 35
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // FIREBASE PLATFORM (BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Firebase Libraries
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    // Tambahkan Library Glide untuk memuat gambar
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Library Retrofit & Gson untuk upload ke ImgBB
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}