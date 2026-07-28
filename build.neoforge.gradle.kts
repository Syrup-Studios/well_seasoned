plugins {
    id("net.neoforged.moddev") version "2.0.137"
    id("maven-publish")
}

val minecraftVersion = property("deps.minecraft") as String
val neoForgeVersion = property("deps.neoforge_version") as String
val modernHud = stonecutter.eval(stonecutter.current.version, ">=1.21.11")
val targetJavaVersion = if (stonecutter.eval(stonecutter.current.version, ">=26")) 25 else 21

version = "${property("mod.version")}+$minecraftVersion-neoforge"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

neoForge {
    version = neoForgeVersion
    runs {
        create("client") { client(); gameDirectory = project.file("run") }
        create("server") {
            server()
            gameDirectory = project.file("run")
            programArgument("--nogui")
        }
    }
    mods.create(property("mod.id") as String) { sourceSet(sourceSets.main.get()) }
}

if (modernHud) {
    sourceSets.main { java.exclude("net/syrupstudios/colorfularmorbar/mixin/**") }
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mc" to minecraftVersion,
        "packFormat" to project.property("deps.resource_pack_format"),
        "neoforge" to neoForgeVersion,
        "modName" to project.property("mod.name"),
        "modId" to project.property("mod.id"),
        "modDescription" to project.property("mod.description"),
        "authors" to project.property("mod.authors"),
        "contributors" to project.property("mod.contributors"),
        "license" to project.property("mod.license"),
        "homepage" to project.property("mod.homepage"),
        "issues" to project.property("mod.issues"),
        "sources" to project.property("mod.sources"),
        "mixinConfig" to if (modernHud) "" else "[[mixins]]\nconfig=\"${project.property("mod.id")}.mixins.json\""
    )
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    filesMatching("pack.mcmeta") { expand(props) }
    if (modernHud) {
        exclude("*.mixins.json")
    } else {
        filesMatching("*.mixins.json") {
            expand("java" to "JAVA_21", "refmapLine" to "")
        }
    }
    exclude("fabric.mod.json", "META-INF/mods.toml")
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("jar"), tasks.named("sourcesJar"))
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    dependsOn("build")
}

apply(from = rootProject.file("gradle/maven-publishing.gradle.kts"))
