plugins {
    alias(libs.plugins.androidApplication)
    }

android {
    namespace = "com.example.app_dizertatie"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.app_dizertatie"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Add Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase libraries
    implementation("com.google.firebase:firebase-auth-ktx") // Authentication
    implementation("com.google.firebase:firebase-firestore-ktx") // Firestore
    implementation("com.google.firebase:firebase-database-ktx") // Realtime Database
    implementation("com.google.firebase:firebase-storage-ktx") // Cloud Storage
}

// Apply Google Services plugin
apply(plugin = "com.google.gms.google-services")
