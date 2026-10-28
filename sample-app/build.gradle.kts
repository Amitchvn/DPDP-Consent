plugins {
    id("com.android.application")
}
android {
    namespace = "com.example.dpdpconsentsample"
    compileSdk = 36
    defaultConfig { applicationId = "com.example.dpdpconsentsample"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
    implementation(project(":dpdp-consent"))
    implementation("androidx.appcompat:appcompat:1.7.1")
}
