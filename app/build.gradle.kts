import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)


}

android {
    namespace = "com.wintercruel.puremusic1"
    compileSdk = 34

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "com.wintercruel.puremusic1"
        minSdk = 31
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
}

dependencies {

    implementation(files("libs\\JTransforms-3.1-with-dependencies.jar"))
    implementation(libs.palette)
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:latest_version")
    implementation(files("libs\\jaudiotagger-3.0.2-SNAPSHOT.jar"))
    implementation(files("libs\\jaudiotagger-3.0.2-SNAPSHOT-javadoc.jar"))

    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.github.zhengken:LyricViewDemo:v1.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("androidx.room:room-runtime:2.5.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.3.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.media:media:1.6.0")


    implementation("com.github.zcweng:switch-button:0.0.3@aar")

    implementation("com.github.bogerchan:Nier-Visualizer:v0.1.3")
    implementation("io.github.gautamchibde:audiovisualizer:2.2.5")
    implementation("io.github.inflationx:calligraphy3:3.1.1")
    implementation("io.github.inflationx:viewpump:2.0.3")
    compileOnly("com.wang.avi:library:2.1.3")

    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}