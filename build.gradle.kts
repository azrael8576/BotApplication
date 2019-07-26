import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("plugin.jpa") version "1.2.71"
    idea
    id("org.springframework.boot") version "2.1.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.41"
    kotlin("plugin.spring") version "1.3.41"
    kotlin("plugin.allopen") version "1.3.41"
    kotlin("kapt") version "1.3.41"
    kotlin("plugin.noarg") version "1.3.41"

}

group = "cc.tapgo"
version = "0.0.1-beta"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.slf4j:slf4j-api:1.7.26")

    implementation("io.github.microutils:kotlin-logging:1.7.1")
//    implementation("io.github.microutils:kotlin-logging-common:1.7.1")

//    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    runtimeOnly("com.h2database:h2:1.4.197") // Fixed version as a workaround for https://github.com/h2database/h2database/issues/1841
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")


    implementation("com.linecorp.bot:line-bot-cli:2.7.0")
    implementation("com.linecorp.bot:line-bot-spring-boot:2.7.0")
    implementation("com.linecorp.bot:line-bot-servlet:2.7.0")
    implementation("com.linecorp.bot:line-bot-model:2.7.0")
    implementation("com.linecorp.bot:line-bot-api-client:2.7.0")


//    implementation("org.springframework.boot:spring-boot-starter-log4j2")
//    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
//    implementation("org.slf4j:slf4j-api:1.7.26")
//    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.0")
//    runtimeOnly("org.apache.logging.log4j:log4j-core:2.12.0")
//    runtimeOnly("org.apache.logging.log4j:log4j-api:2.12.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

//allOpen {
//    annotation("javax.persistence.Entity")
//    annotation("javax.persistence.Embeddable")
//    annotation("javax.persistence.MappedSuperclass")
//}

tasks.withType<Test> {
    useJUnitPlatform()
}
