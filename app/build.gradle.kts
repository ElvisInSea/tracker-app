import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.tracker.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tracker.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // 从 local.properties 读取签名配置
            val keystorePropertiesFile = rootProject.file("local.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                FileInputStream(keystorePropertiesFile).use {
                    keystoreProperties.load(it)
                }
                
                val keystorePath = keystoreProperties.getProperty("keystore.path") ?: ""
                // 如果路径是相对路径，则相对于项目根目录
                storeFile = if (keystorePath.isNotEmpty()) {
                    val keystoreFile = rootProject.file(keystorePath)
                    if (keystoreFile.exists()) keystoreFile else file(keystorePath)
                } else {
                    file("")
                }
                storePassword = keystoreProperties.getProperty("keystore.password") ?: ""
                keyAlias = keystoreProperties.getProperty("keystore.alias") ?: ""
                keyPassword = keystoreProperties.getProperty("keystore.keyPassword") ?: ""
            }
        }
    }

    buildTypes {
        // Debug 构建类型（开发阶段使用）
        // 默认配置：不混淆、不压缩资源、使用debug签名、BuildConfig.DEBUG = true
        debug {
            // Debug版本保持默认配置，便于开发和调试
            // - isMinifyEnabled = false (默认)
            // - isShrinkResources = false (默认)
            // - BuildConfig.DEBUG = true (默认)
            // LogUtils 会根据 BuildConfig.DEBUG 自动输出所有日志
        }
        
        // Release 构建类型（发布时使用）
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true  // 仅Release版本启用代码混淆
            isShrinkResources = true  // 仅Release版本启用资源压缩
            // BuildConfig.DEBUG = false (默认)
            // LogUtils 在Release版本中只输出错误日志
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    
    // Date picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    
    // Charts (使用 Vico 图表库，轻量且美观)
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")
    
    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

