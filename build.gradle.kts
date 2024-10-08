plugins {
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
}

group = "com.infrastructure"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	implementation("com.github.Mixfaa:excify:0.0.2")
    ksp("com.github.Mixfaa:excify:0.0.2")
	implementation("com.github.Mixfaa:excify-either-module:0.0.2")

	implementation("io.arrow-kt:arrow-core:1.2.4")
	implementation("io.arrow-kt:arrow-fx-coroutines:1.2.4")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.projectreactor:reactor-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

