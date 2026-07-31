import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17.14" apply false
    id("net.fabricmc.fabric-loom") version "1.17.14" apply false
    id("maven-publish")
}

val remappedMinecraft = stonecutter.eval(stonecutter.current.version, "<26")
val minecraftVersion = property("deps.minecraft") as String
val targetJavaVersion = if (stonecutter.eval(stonecutter.current.version, ">=26")) 25 else 21
val requiredJava = JavaVersion.toVersion(targetJavaVersion)

apply(plugin = if (remappedMinecraft) "net.fabricmc.fabric-loom-remap" else "net.fabricmc.fabric-loom")

version = "${property("mod.version")}+$minecraftVersion-fabric"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val loomExtension = extensions.getByType<LoomGradleExtensionAPI>()

dependencies {
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    if (remappedMinecraft) add("mappings", loomExtension.officialMojangMappings())

    val modConfiguration = if (remappedMinecraft) "modImplementation" else "implementation"
    add(modConfiguration, "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    add(modConfiguration, "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

loomExtension.apply {
    fabricModJsonPath.set(rootProject.file("src/main/resources/fabric.mod.json"))
    if (remappedMinecraft) {
        decompilerOptions.named("vineflower") {
            options.put("mark-corresponding-synthetics", "1")
        }
    }
    runConfigs.configureEach { runDir = "run" }
}

java {
    withSourcesJar()
    withJavadocJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

val fabricMetadataSource = rootProject.file("src/main/resources/fabric.mod.json")
val generatedFabricMetadata = layout.buildDirectory.file("generated/fabricMetadata/fabric.mod.generated.json")
val fabricResourceProperties = mapOf(
    "version" to project.version,
    "mc" to minecraftVersion,
    "packFormat" to project.property("deps.resource_pack_format"),
    "modName" to project.property("mod.name"),
    "modId" to project.property("mod.id"),
    "modDescription" to project.property("mod.description"),
    "authors" to project.property("mod.authors"),
    "contributors" to project.property("mod.contributors"),
    "homepage" to project.property("mod.homepage"),
    "issues" to project.property("mod.issues"),
    "sources" to project.property("mod.sources"),
    "license" to project.property("mod.license"),
    "fl" to project.property("deps.fabric_loader"),
    "fapi" to project.property("deps.fabric_api")
)
val generateFabricMetadata = tasks.register("generateFabricMetadata") {
    inputs.file(fabricMetadataSource)
    inputs.property("contributors", fabricResourceProperties.getValue("contributors"))
    outputs.file(generatedFabricMetadata)
    doLast {
        @Suppress("UNCHECKED_CAST")
        val metadata = JsonSlurper().parse(fabricMetadataSource) as MutableMap<String, Any?>
        if (fabricResourceProperties.getValue("contributors").toString().isBlank()) {
            metadata.remove("contributors")
        }

        generatedFabricMetadata.get().asFile.apply {
            parentFile.mkdirs()
            writeText(JsonOutput.prettyPrint(JsonOutput.toJson(metadata)) + "\n")
        }
    }
}

tasks.processResources {
    dependsOn(generateFabricMetadata)
    inputs.properties(fabricResourceProperties)
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
    from(generatedFabricMetadata) {
        rename { "fabric.mod.json" }
        expand(fabricResourceProperties)
    }
    filesMatching("pack.mcmeta") { expand(fabricResourceProperties) }

    filesMatching("*.mixins.json") {
        expand("java" to "JAVA_$targetJavaVersion")
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    val productionJar = if (remappedMinecraft) "remapJar" else "jar"
    val sourceJar = if (remappedMinecraft) "remapSourcesJar" else "sourcesJar"
    from(tasks.named(productionJar), tasks.named(sourceJar))
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    dependsOn("build")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

apply(from = rootProject.file("gradle/maven-publishing.gradle.kts"))
