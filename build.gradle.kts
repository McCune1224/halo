plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "io.mckuna"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
}
