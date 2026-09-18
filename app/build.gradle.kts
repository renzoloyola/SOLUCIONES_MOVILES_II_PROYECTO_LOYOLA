import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use(::load)
    }
}

fun configValue(name: String, default: String = ""): String {
    return providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orElse(localProperties.getProperty(name, default))
        .get()
}

val marketplaceBackendUrlValue = configValue(name = "MARKETPLACE_BACKEND_URL")
val marketplaceBackendConfigured = marketplaceBackendUrlValue.isNotBlank()
val marketplaceBackendUrl = marketplaceBackendUrlValue
    .ifBlank { "https://changescout-backend.invalid/" }
    .let { value ->
        val url = value.trim()
        if (url.endsWith("/")) url else "$url/"
    }
val supabaseUrl = configValue("SUPABASE_URL")
val supabasePublishableKey = configValue("SUPABASE_PUBLISHABLE_KEY")

require(
    supabasePublishableKey.isBlank() || supabasePublishableKey.startsWith("sb_publishable_")
) {
    "SUPABASE_PUBLISHABLE_KEY debe ser una clave publica sb_publishable_. " +
        "Nunca uses sb_secret_, service_role ni otra clave privada en Android."
}

fun String.asBuildConfigString(): String {
    return "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

android {
    namespace = "com.app.changescout"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.app.changescout"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "MARKETPLACE_BACKEND_URL", marketplaceBackendUrl.asBuildConfigString())
            buildConfigField("boolean", "MARKETPLACE_BACKEND_CONFIGURED", marketplaceBackendConfigured.toString())
            buildConfigField("String", "SUPABASE_URL", supabaseUrl.asBuildConfigString())
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", supabasePublishableKey.asBuildConfigString())
        }
        release {
            buildConfigField("String", "MARKETPLACE_BACKEND_URL", marketplaceBackendUrl.asBuildConfigString())
            buildConfigField("boolean", "MARKETPLACE_BACKEND_CONFIGURED", marketplaceBackendConfigured.toString())
            buildConfigField("String", "SUPABASE_URL", supabaseUrl.asBuildConfigString())
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", supabasePublishableKey.asBuildConfigString())
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions.unitTests.all {
        it.testLogging {
            events("passed", "failed", "skipped")
            showStandardStreams = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
