plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)


}

android {
    namespace = "com.example.letstalk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.letstalk"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes.add("META-INF/androidx.localbroadcastmanager_localbroadcastmanager.version")
        }
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("de.hdodenhof:circleimageview:2.2.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")

    implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")

    implementation("com.firebaseui:firebase-ui-database:8.0.0")



    implementation("com.squareup.picasso:picasso:2.8")
}