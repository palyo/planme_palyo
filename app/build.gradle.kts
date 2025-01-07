plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.firebase.crashlitycs)
    alias(libs.plugins.gms.googleServices)
}

android {
    namespace = "aurora.reminder.todolist.calendar"
    compileSdk = 35

    defaultConfig {
        applicationId = "aurora.reminder.todolist.calendar.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-beta"

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        setProperty("archivesBaseName", "PlanMe v$versionName")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    dataBinding.enable = true
    buildTypes {
        debug {
            isMinifyEnabled = true
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    bundle {
        language {
            enableSplit = false
        }
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/NOTICE.md")
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    configurations {
        implementation {
            exclude(group = "com.squareup.okio", module = "okio")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    implementation(libs.codespace)

    implementation(libs.lottie)
    implementation(libs.gson)
    implementation(libs.multidex)
    implementation(libs.preference)
    implementation(libs.browser)

    implementation(libs.work.runtime)

    implementation(libs.bundles.glide)
    ksp(libs.glide.ksp)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.lifecycle)
    implementation(libs.livedata.ktx)
    implementation(libs.bundles.coroutines)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.messaging.ktx)

    implementation("com.google.android.ump:user-messaging-platform:2.2.0")
    implementation("com.calldorado:calldorado-release:8.1.8.3839@aar") {
        isTransitive = true
    }
    implementation("com.applovin:applovin-sdk:12.6.0")
    implementation("com.applovin.mediation:vungle-adapter:7.4.0.0")

    implementation("com.facebook.android:audience-network-sdk:6.17.0")
    implementation("com.applovin.mediation:facebook-adapter:6.17.0.0")
    implementation("com.google.ads.mediation:facebook:6.17.0.0")

    implementation("com.google.android.gms:play-services-ads:23.2.0")
    implementation("com.applovin.mediation:google-ad-manager-adapter:23.2.0.0")
    implementation("com.applovin.mediation:google-adapter:23.2.0.0")
}