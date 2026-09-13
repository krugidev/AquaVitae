plugins {
    // Deixa o Gradle descarregar automaticamente um JDK 21 para compilar/correr
    // este projeto, mesmo que o JDK instalado no sistema seja outra versão
    // (ex.: JDK 25) — evita depender de teres exatamente Java 21 instalado.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "aquavitae-api"
