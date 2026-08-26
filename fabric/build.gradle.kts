
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    id("dev.kikugie.loom-back-compat")
    id("com.google.devtools.ksp") version "2.3.4"
    `maven-publish`
    id("me.modmuss50.mod-publish-plugin")
    id("org.jetbrains.dokka") version "2.1.0"
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String
group = "llc.redstone"

val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    maven("https://repo.redstone.llc/releases")
    maven("https://maven.kosmx.dev") //IDK why I couldnt make this a strict maven :shrug:
    maven("https://maven.wispforest.io/releases")
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    maven("https://jitpack.io")

    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.terraformersmc.com/", "Terraformers")
    strictMaven("https://maven.isxander.dev/releases", "Xander Maven")
    strictMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "DevAuth")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.fabric_language_kotlin")}")
    modImplementation("io.wispforest:owo-lib:${property("deps.owo")}")
    modCompileOnly("maven.modrinth:dynamic-fps:${property("deps.dynamic_fps")}")
    ksp("dev.kosmx.kowoconfig:ksp-owo-config:0.2.0")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    implementation(include("llc.redstone:SystemsData:1.2.1")!!)

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
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
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(requiredJava.majorVersion))
    }
}

publishMods {
    file.set(loomx.modJar.flatMap { it.archiveFile })
    displayName.set("${property("mod.name")} ${property("mod.version")} for ${property("mod.mc_title")}")
    version.set(property("mod.version") as String)
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    type.set(BETA)
    modLoaders.add("fabric")

    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId.set(property("publish.modrinth") as String)
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))

        requires("fabric-language-kotlin", "owo-lib", "fabric-api")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "SystemsAPI"

            version = if (hasProperty("commit")) "${property("commit")}+${stonecutter.current.version}" else project.version.toString()
//            version = "dev"
        }
    }
    repositories {
        maven {
            name = "releasesRepo"
            url = uri("https://repo.redstone.llc/releases")
            credentials {
                username = findProperty("releasesRepoUsername") as? String
                password = findProperty("releasesRepoPassword") as? String
            }
        }
        maven {
            name = "snapshotsRepo"
            url = uri("https://repo.redstone.llc/snapshots")
            credentials {
                username = findProperty("releasesRepoUsername") as? String
                password = findProperty("releasesRepoPassword") as? String
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
}
