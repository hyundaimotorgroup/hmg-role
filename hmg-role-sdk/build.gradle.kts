val jakartaValidationVersion = "2.0.2"
val opencsvVersion = "5.12.0"
val mapdbVersion = "3.1.0"
val kotlinVersion = "2.4.20"
val nettyVersion = "4.2.16.Final"
val awsSdkBomVersion = "2.50.2"
val jacksonBomVersion = "2.22.1"
val okhttpVersion = "4.12.0"
val junitBomVersion = "5.10.0"
val lombokVersion = "1.18.32"
val plexusUtilsVersion = "3.6.1"

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    // signing // is this required?
}

group = "com.hmg.role"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        // required by gradle during compilation
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// mapdb:3.1.0 restricts kotlin to [1.9.22,1.9.999), downgrading okhttp/okio which want 2.x.
// kotlin-stdlib < 2.1.0 has an Information Exposure CVE; no newer MapDB available.
// TODO remove once MapDB ships a release compatible with Kotlin 2.x.
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(kotlinVersion)
                because("kotlin < 2.1.0 Information Exposure CVE; MapDB 3.1.0 is unmaintained and caps at 1.9.x")
            }
            if (requested.group == "io.netty") {
                useVersion(nettyVersion)
                because("Netty 4.1.x EOL; 4.1.135.Final has CVE; forcing to 4.2.15.Final (accepted risk)")
            }
            if (requested.group == "com.fasterxml.jackson.core") {
                useVersion(jacksonBomVersion)
                because("Netty 4.1.x EOL; 4.1.135.Final has CVE; forcing to 4.2.15.Final (accepted risk)")
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = group
    }
}

dependencies {
    constraints {
        // maven-artifact (pulled via debezium-embedded → connect-runtime) brings plexus-utils 3.5.1 (CVE)
        // TODO remove after debezium sobers up and bump the dependency
        implementation("org.codehaus.plexus:plexus-utils:$plexusUtilsVersion")
    }

    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")

    // TODO: consider to use lightweight alternative, perhaps just DIY
    implementation("com.opencsv:opencsv:$opencsvVersion") {
        exclude(group = "commons-collections")
    }

    implementation("org.mapdb:mapdb:$mapdbVersion")

    // TODO: consider to use minio client as lightweight alternative
    implementation(platform("software.amazon.awssdk:bom:$awsSdkBomVersion"))
    implementation("software.amazon.awssdk:s3")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonBomVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    // JUnit BOM
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // required in unit tests
    testImplementation("org.projectlombok:lombok:$lombokVersion")
}

tasks.test {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )

    useJUnitPlatform()
}

