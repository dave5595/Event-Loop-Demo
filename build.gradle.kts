import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(platform("net.openhft:chronicle-bom:2.23ea68"))
    implementation(platform("net.openhft:third-party-bom:3.19.1"))
    implementation("net.openhft:affinity")
    implementation("net.openhft:chronicle-queue")
    implementation("net.openhft:chronicle-map")
    implementation("net.openhft:jlbh")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}