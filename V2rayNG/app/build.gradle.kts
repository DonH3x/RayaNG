plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  id("com.jaredsburrows.license")
}

android {
  namespace = "com.raya.v2ray"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.raya.v2ray"
    minSdk = 21
    targetSdk = 35
    versionCode = 4
    versionName = "1.2.1"
    multiDexEnabled = true

    val abiFilterList = (properties["ABI_FILTERS"] as? String)?.split(';')
    splits {
      abi {
        isEnable = true
        reset()
        if (abiFilterList != null && abiFilterList.isNotEmpty()) {
          include(*abiFilterList.toTypedArray())
        } else {
          include(
            "arm64-v8a",
            "armeabi-v7a",
            "x86_64",
            "x86"
          )
        }
        isUniversalApk = abiFilterList.isNullOrEmpty()
      }
    }

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

  flavorDimensions.add("distribution")
  productFlavors {
    create("playstore") {
      dimension = "distribution"
      buildConfigField("String", "DISTRIBUTION", "\"Play Store\"")
    }
  }

  sourceSets {
    getByName("main") {
      jniLibs.srcDirs("libs")
    }
  }


  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }

  applicationVariants.all {
    val variant = this
    val versionCodes =
      mapOf("armeabi-v7a" to 4, "arm64-v8a" to 4, "x86" to 4, "x86_64" to 4, "universal" to 4)

    variant.outputs
      .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
      .forEach { output ->
        val abi = if (output.getFilter("ABI") != null)
          output.getFilter("ABI")
        else
          "universal"

        output.outputFileName = "RayaNG_${variant.versionName}_${abi}.apk"
        if (versionCodes.containsKey(abi)) {
          output.versionCodeOverride =
            (1000000 * versionCodes[abi]!!).plus(variant.versionCode)
        } else {
          return@forEach
        }
      }
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
  }

  packaging {
    jniLibs {
      useLegacyPackaging = true
    }
  }

}

dependencies {
  // Core Libraries
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

  // AndroidX Core Libraries
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.preference.ktx)
  implementation(libs.recyclerview)
  implementation(libs.androidx.swiperefreshlayout)

  // UI Libraries
  implementation(libs.material)
  implementation(libs.toasty)
  implementation(libs.editorkit)
  implementation(libs.flexbox)

  // Data and Storage Libraries
  implementation(libs.mmkv.static)
  implementation(libs.gson)

  // Reactive and Utility Libraries
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  // Language and Processing Libraries
  implementation(libs.language.base)
  implementation(libs.language.json)

  // Intent and Utility Libraries
  implementation(libs.quickie.foss)
  implementation(libs.core)

  // AndroidX Lifecycle and Architecture Components
  implementation(libs.lifecycle.viewmodel.ktx)
  implementation(libs.lifecycle.livedata.ktx)
  implementation(libs.lifecycle.runtime.ktx)

  // Background Task Libraries
  implementation(libs.work.runtime.ktx)
  implementation(libs.work.multiprocess)

  // Multidex Support
  implementation(libs.multidex)

  // Testing Libraries
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  testImplementation(libs.org.mockito.mockito.inline)
  testImplementation(libs.mockito.kotlin)
  coreLibraryDesugaring(libs.desugar.jdk.libs)
}
