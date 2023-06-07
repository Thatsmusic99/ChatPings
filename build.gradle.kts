/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("com.modrinth.minotaur") version "2.+"
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.essentialsx.net/releases/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.2")
}

group = "groupId"
version = "2.5"
description = "ChatPings"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("WfG6niqV")
    versionNumber.set(version.toString())
    versionType.set("release")
    uploadFile.set(tasks.jar.get())
    gameVersions.addAll(arrayListOf("1.8", "1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.18.1", "1.18.2", "1.19", "1.19.1", "1.19.2"))
    loaders.addAll(arrayListOf("paper", "spigot"))
}
