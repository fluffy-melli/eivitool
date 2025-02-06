plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

group = "io.github.fluffy-melli.Eivitool"
version = "0.1.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jcodec:jcodec:0.2.5")
    implementation("org.jcodec:jcodec-javase:0.2.5")
    implementation("org.bytedeco:javacv:1.5.9") {
        exclude(group = "org.bytedeco", module = "javacv-platform")
    }
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9")
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}

tasks {
    shadowJar {
        mergeServiceFiles()
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

        manifest {
            attributes["Main-Class"] = "eivitool.MainKt"
        }

        from("src/main/resources") {
            include("**/*.properties")
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
