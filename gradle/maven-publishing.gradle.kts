import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.BasicAuthentication

val mavenRepositoryUrl = providers.gradleProperty("mavenRepositoryUrl")
    .orElse(providers.environmentVariable("MAVEN_REPOSITORY_URL"))
    .orElse("https://maven.syrupstudios.net/releases/")
val mavenRepositoryUsername = providers.gradleProperty("mavenRepositoryUsername")
    .orElse(providers.environmentVariable("MAVEN_REPOSITORY_USERNAME"))
val mavenRepositoryPassword = providers.gradleProperty("mavenRepositoryPassword")
    .orElse(providers.environmentVariable("MAVEN_REPOSITORY_PASSWORD"))
val archiveName = extensions.getByType<BasePluginExtension>().archivesName.get()
val javaComponent = components["java"]
val projectHomepage = project.property("mod.homepage") as String
val projectIssues = project.property("mod.issues") as String
val projectSources = (project.property("mod.sources") as String).removeSuffix("/")

extensions.configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(javaComponent)
            artifactId = archiveName

            pom {
                name.set("${project.property("mod.name")} ($archiveName)")
                description.set(project.property("mod.description") as String)
                url.set(projectHomepage)

                licenses {
                    license {
                        name.set(project.property("mod.license") as String)
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("SodaSyrup")
                        name.set(project.property("mod.authors") as String)
                        organization.set("Syrup Studios")
                        organizationUrl.set("https://github.com/Syrup-Studios")
                    }
                }
                scm {
                    connection.set("scm:git:$projectSources.git")
                    developerConnection.set("scm:git:$projectSources.git")
                    url.set(projectSources)
                }
                issueManagement {
                    system.set("GitHub")
                    url.set(projectIssues)
                }
            }
        }
    }

    repositories {
        maven {
            name = "syrupStudios"
            url = uri(mavenRepositoryUrl.get())
            if (mavenRepositoryUsername.isPresent || mavenRepositoryPassword.isPresent) {
                credentials {
                    username = mavenRepositoryUsername.orNull
                    password = mavenRepositoryPassword.orNull
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
