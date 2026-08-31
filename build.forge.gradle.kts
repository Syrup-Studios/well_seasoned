plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.137"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
    id("maven-publish")
}

val mcVersionValue = property("deps.minecraft") as String
val forgeVersionValue = property("deps.forge_version") as String
val targetJavaVersion = 17
val requiredJava = JavaVersion.VERSION_17
val modId = property("mod.id") as String

version = "${property("mod.version")}+$mcVersionValue-forge"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

repositories {
    maven("https://maven.syrupstudios.net/releases/")
}

legacyForge {
    setVersion("$mcVersionValue-$forgeVersionValue")
    runs {
        create("client") {
            client()
            gameDirectory = project.file("run")
        }
        create("server") {
            server()
            gameDirectory = project.file("run")
            programArgument("--nogui")
        }
    }
    mods.create(modId) { sourceSet(sourceSets.main.get()) }
}

sourceSets.main {
    java.exclude("net/syrupstudios/wellseasoned/loaders/fabric/**")
}

dependencies {
    implementation("net.syrupstudios:syrup_library:${property("deps.syrup_library")}")
    jarJar("net.syrupstudios:syrup_library:${property("deps.syrup_library")}")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mc" to mcVersionValue,
        "packFormat" to project.property("deps.resource_pack_format"),
        "forge" to forgeVersionValue,
        "modName" to project.property("mod.name"),
        "modId" to project.property("mod.id"),
        "modDescription" to project.property("mod.description"),
        "authors" to project.property("mod.authors"),
        "contributors" to project.property("mod.contributors"),
        "license" to project.property("mod.license"),
        "homepage" to project.property("mod.homepage"),
        "issues" to project.property("mod.issues"),
        "sources" to project.property("mod.sources")
    )
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") { expand(props) }
    filesMatching("pack.mcmeta") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_$targetJavaVersion") }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
}

tasks.jar {
    manifest.attributes["MixinConfigs"] = "$modId.mixins.json"
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("jar"), tasks.named("sourcesJar"))
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    dependsOn("build")
}

apply(from = rootProject.file("gradle/maven-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/platform-publishing.gradle"))
