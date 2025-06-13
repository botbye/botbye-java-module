import java.time.LocalDate
import org.jreleaser.model.Active

plugins {
    `maven-publish`
    id("java-library")
    signing
    id("java")
    id("org.jreleaser") version "1.18.0"
}

group = "com.botbye"
version = "0.0.1"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.15.3")
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
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    release {
        github {
            skipRelease = true
            skipTag = true
        }
    }

    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
    }

    project {
        inceptionYear = "${LocalDate.now().year}"
        author("@botbye")
    }

    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                setAuthorization("Basic")
                retryDelay = 60
                sign = true
                checksums = true
                sourceJar = true
                javadocJar = true
            }
        }
    }
}

