/*
 *   Copyright © 2021-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

plugins {
    id("com.android.application")
}

android {
    namespace = "com.pspdfkit.instant.example"
    compileSdk = 35

    defaultConfig {
        applicationId = namespace
        minSdk = 26
        targetSdk = compileSdk

        versionName = "10.1.0"
        versionCode = 142256

        resValue("string", "NUTRIENT_LICENSE_KEY", "\"LICENSE_KEY_GOES_HERE\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }


}

dependencies {
    // Nutrient is integrated from the Nutrient Maven repository. See the `repositories` block at the beginning
    // of this file, which shows how to set up the repository in your app.
    implementation("io.nutrient:nutrient:10.1.0")

    // Retrofit and Gson for web example REST API.
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
