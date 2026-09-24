import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.spring") version "2.4.20"
    kotlin("plugin.jpa") version "2.4.20"
}

group = "pt.aquavitae"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Envio de email por SMTP (código de recuperação de password); sem SPRING_MAIL_HOST não há servidor e o envio fica em log (só em dev)
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // Painel de administração web (/admin): páginas no servidor com Thymeleaf + HTMX (servido do webjar, sem CDN nem build de
    // front-end). O `locator` deixa referenciar o ficheiro sem a versão no URL (/webjars/htmx.org/dist/htmx.min.js).
    // htmx 2.0.x = a linha estável (o `latest` do npm); a 4.x ainda é `next`.
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.webjars.npm:htmx.org:2.0.11")
    implementation("org.webjars:webjars-locator-lite:1.1.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Oracle JDBC driver (liga ao Oracle XE do docker-compose em ../database)
    runtimeOnly("com.oracle.database.jdbc:ojdbc11:21.9.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
