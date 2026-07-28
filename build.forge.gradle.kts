plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.137"
    id("maven-publish")
}

val mcVersion = property("deps.minecraft") as String
val forgeVersion = property("deps.forge_version") as String
val targetJavaVersion = 17

version = "${property("mod.version")}+$mcVersion-forge"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

legacyForge {
    setVersion("$mcVersion-$forgeVersion")
    runs {
        create("client") {
            client()
            gameDirectory = project.file("run")
        }
    }
    mods.create(property("mod.id") as String) { sourceSet(sourceSets.main.get()) }
}

sourceSets.main {
    java.exclude("net/syrupstudios/colorfularmorbar/mixin/**")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mc" to mcVersion,
        "packFormat" to project.property("deps.resource_pack_format"),
        "forge" to forgeVersion,
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
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("jar"), tasks.named("sourcesJar"))
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    dependsOn("build")
}

apply(from = rootProject.file("gradle/maven-publishing.gradle.kts"))
