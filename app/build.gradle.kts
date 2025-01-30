plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")    // For annotation processing if needed
    id("androidx.navigation.safeargs.kotlin") version "2.8.3"

}

android {
    namespace = "com.tsa.bmicalculator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tsa.bmicalculator"
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true // Corrected here
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.transition:transition:1.5.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation ("androidx.fragment:fragment-ktx:1.8.5")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.1")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.1")
    implementation ("androidx.core:core-ktx:1.10.0")
    implementation ("androidx.activity:activity-ktx:1.9.3")
    implementation ("com.airbnb.android:lottie:6.6.2")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.work:work-runtime-ktx:2.9.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.2.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.0")
}