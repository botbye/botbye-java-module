plugins {
    `maven-publish`
    id("java-library")
    signing
    id("java")
    id("io.codearte.nexus-staging") version "0.30.0"
}

group = "com.botbye"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.15.3")
}

tasks {
    register<Javadoc>("withJavadoc")

    register<Jar>("withJavadocJar") {
        archiveClassifier.set("javadoc")
        dependsOn(named("withJavadoc"))
        val destination = named<Javadoc>("withJavadoc").get().destinationDir
        from(destination)
    }

    register<Jar>("withSourcesJar") {
        archiveClassifier.set("sources")
        from(project.sourceSets.getByName("main").java.srcDirs)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "java-module"
            from(components["java"])
            pom {
                packaging = "jar"
                name.set("BotBye Java module")
                url.set("https://github.com/botbye/botbye-java-module")
                description.set("Java module for integration with botbye.com")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/botbye/botbye-java-module.git")
                    developerConnection.set("scm:git@github.com:botbye/botbye-java-module.git")
                    url.set("https://botbye.com/")
                }

                developers {
                    developer {
                        id.set("BotBye")
                        name.set("BotBye")
                        email.set("https://botbye.com/")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = project.properties["ossrhUsername"].toString()
                password = project.properties["ossrhPassword"].toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    username = project.properties["ossrhUsername"].toString()
    password = project.properties["ossrhPassword"].toString()
}
