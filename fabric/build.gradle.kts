plugins {
    kotlin("jvm") version "2.2.10"
    id("fabric-loom")
    `maven-publish`
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String
group = "llc.redstone"

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.terraformersmc.com/", "Terraformers")

    strictMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "DevAuth")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.fabric_language_kotlin")}")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-core:2.22.0")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    val javaVersion: JavaVersion = JavaVersion.VERSION_21
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "SystemsAPI"
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "releasesRepo"
            url = uri("https://repo.redstone.llc/releases")
            credentials {
                username = property("releasesRepoUsername") as String
                password = property("releasesRepoPassword") as String
            }
        }
    }
}

tasks {
    processResources {
        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "fabric_loader" to project.property("deps.fabric_loader"),
            "fabric_language_kotlin" to project.property("deps.fabric_language_kotlin")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}
