/*
 *   Copyright © 2021-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

plugins {
    id("com.android.application")
}

android {
    namespace = "com.pspdfkit.instant.example"
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        applicationId = namespace
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionName = "2024.1.2"
        versionCode = 138349

        resValue("string", "PSPDFKIT_LICENSE_KEY", "\"LICENSE_KEY_GOES_HERE\"")
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
    // PSPDFKit is integrated from the PSPDFKit Maven repository. See the `repositories` block at the beginning
    // of this file, which shows how to set up the repository in your app.
    implementation("com.pspdfkit:pspdfkit:2024.1.2")
    // Retrofit and Gson for web example REST API.
    implementation(libs.gson)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.retrofit.rxjava3)
}
