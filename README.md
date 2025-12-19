<h1 align="center">
    <img src="fabric/src/main/resources/assets/systemsapi/icon.png" height="256" alt="SystemsAPI Icon">
    <br>
    SystemsAPI
    <br>
    <a href="https://github.com/redstone-llc/SystemsAPI/commits/main/">
        <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/redstone-llc/SystemsAPI?style=for-the-badge&logo=github&logoColor=%23cad3f5&labelColor=%23363a4f&color=%2340a02b">
    </a>
    <a href="https://repo.redstone.llc/#/releases/llc/redstone/SystemsAPI">
        <img alt="Maven Version" src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.redstone.llc%2Freleases%2Fllc%2Fredstone%2FSystemsAPI%2Fmaven-metadata.xml&style=for-the-badge&logo=Apache%20Maven&logoColor=%23cad3f5&labelColor=%23363a4f&color=%238839ef">
    </a>
    <br>
    <a href="https://discord.gg/pCcpqzU4He">
        <img alt="By Redstone Studios" src="https://img.shields.io/badge/By-Redstone%20Studios-red?style=for-the-badge&labelColor=%23363a4f&color=%23e64553">
    </a>
</h1>

## Overview

SystemsAPI is a fabric library that abstracts Hypixel Housing's house systems for other mods to use. For an example implementation, see [HTSL Reborn](https://github.com/sinender/HTSLReborn).

> [!WARNING]
> This project is a work in progress! Expect constant changes to API structure at this stage.

## Features

- [x] Action importing
  - [x] Error correction (catches when things don't go how they should, and tries again automatically)
  - [ ] Optimized importing (optimally keeps/changes existing actions to make imports quick!)
- [x] Action exporting
- [x] System abstraction (methods for programmatically interacting with any/all Housing systems.)
    - [x] Functions
    - [x] Events
    - [x] Commands
    - [x] Custom Menus
    - [x] Regions
    - [x] Scoreboard
    - [x] Teams
    - [ ] NPCs
    - [x] Inventory Layouts
    - [ ] House Settings
    - [ ] Roles
- [ ] Scope and limit abstraction, for easy action validation

## Usage

To use SystemsAPI in your mod, include the project in your maven/gradle project. Replace `VERSION` with the latest version, as shown in the Maven badge above (omit the `V`).

### Gradle
```groovy
repositories {
    maven {
        name = "redstoneReleases"
        url = uri("https://repo.redstone.llc/releases")
    }
}

dependencies {
    modImplementation("llc.redstone:SystemsAPI:VERSION")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>redstone-releases</id>
        <name>Redstone Releases</name>
        <url>https://repo.redstone.llc/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>llc.redstone</groupId>
        <artifactId>SystemsAPI</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```
