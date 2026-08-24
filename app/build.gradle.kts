plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "uy.horacio.sueldoliquidouy"
    compileSdk = 36

    defaultConfig {
        applicationId = "uy.horacio.sueldoliquidouy"
        minSdk = 26 // Minimo que se ejecute en Android 8
        targetSdk = 36 // Y que funcione bien en Android 16
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true // BuildConfig.VERSION_NAME lo usa la pantalla de configuración
    }
}

kotlin {
    // Fija el JDK de compilación en vez de heredar el del entorno. Ver la nota
    // en gradle.properties: es lo que permite compilar indistintamente desde
    // Android Studio (flatpak) y desde la terminal.
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Guardar datos que sobreviven al cierre de la app
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Navegación entre pantallas
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Íconos de Material. Material 3 dejó de arrastrarlos de forma transitiva.
    // Sin versión: la maneja el BOM de Compose.
    implementation("androidx.compose.material:material-icons-extended")

    // collectAsStateWithLifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    // Los parámetros fiscales se guardan como un único JSON en DataStore
    implementation(libs.kotlinx.serialization.json)


}