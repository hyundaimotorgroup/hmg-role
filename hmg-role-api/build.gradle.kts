val javersVersion = "7.7.0"
val plexusUtilsVersion = "3.6.1"
val redissonVersion = "3.52.0"
val springdocBomVersion = "2.8.17"
val liquibaseVersion = "4.27.0"
val springCloudAwsVersion = "3.2.1"
val mapstructVersion = "1.5.5.Final"
val cucumberVersion = "7.18.0"
val restAssuredVersion = "5.5.0"
val slf4jVersion = "2.0.17"
val logstashVersion = "7.4"
val awsSdkBomVersion = "2.50.2"
val debeziumBomVersion = "3.6.0.Final"
val assertjVersion = "3.27.7"
val jacksonBomVersion = "2.22.1"
val kotlinVersion = "2.4.20"
val nettyVersion = "4.2.16.Final"
val commonsLang3Version = "3.20.0"

// will be honored by spring boot and black duck
// adjust as needed
ext["tomcat.version"] = "10.1.57"
ext["jackson-bom.version"] = jacksonBomVersion
// spring boot 3.5.x manages commons-lang3 at 3.17.0 (CVE-2025-48924); opencsv and swagger already
// request 3.18.0+ but the BOM downgrades them — override to latest.
ext["commons-lang3.version"] = commonsLang3Version

plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// mapdb:3.1.0 (via hmg-role-sdk) restricts kotlin to [1.9.22,1.9.999), downgrading okhttp/okio which want 2.x.
// kotlin-stdlib < 2.1.0 has an Information Exposure CVE; no newer MapDB available.
// TODO remove once MapDB ships a release compatible with Kotlin 2.x.
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(kotlinVersion)
                because("kotlin < 2.1.0 Information Exposure CVE; MapDB 3.1.0 is unmaintained and caps at 1.9.x")
            }
            // Netty 4.1.x branch is EOL; 4.1.135.Final is the last 4.1.x release.
            // AWS SDK 2.46.x still BOM-constrains Netty to 4.1.135.Final (CVE).
            // 4.2.x is the only available fix; major version — accepted risk.
            // TODO remove once AWS SDK ships a version that aligns with Netty 4.2.x natively.
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

configurations.compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
}

dependencies {
    constraints {
        // maven-artifact (pulled via debezium-embedded → connect-runtime) brings plexus-utils 3.5.1 (CVE)
        // debezium 3.6.0.Final still affected; TODO remove once debezium ships a clean maven-artifact version
        implementation("org.codehaus.plexus:plexus-utils:$plexusUtilsVersion")
    }

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.javers:javers-spring-boot-starter-sql:$javersVersion")
    implementation("org.redisson:redisson-hibernate-6:$redissonVersion")

    // springdoc 2.8.17 is explicitly tested against Spring Boot 3.5.13 (exact match).
    // swagger-core (pulled transitively) does not pin a jackson version — it uses
    // whatever Spring Boot manages (via ext["jackson-bom.version"] = jacksonBomVersion).
    implementation(platform("org.springdoc:springdoc-openapi-bom:$springdocBomVersion"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    testImplementation("com.h2database:h2")

    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:$springCloudAwsVersion"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-secrets-manager")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.assertj")
    }
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.platform:junit-platform-suite")

    testImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion") // Works with Java 8+
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // hmg-role-sdk is consumed as a project dependency. Gradle resolves its full
    // transitive tree (not just the shadow JAR), so its declared deps become visible here.
    //
    // jackson-core: excluded because the SDK hardcodes a jackson version; Spring Boot's
    //   dependency management (jackson-bom:2.21.2) must own all jackson artifacts.
    //
    // net.jpountz.lz4:lz4: mapdb (used internally by the SDK) pulls the abandoned
    //   net.jpountz.lz4:lz4:1.3.0 (last released 2014). debezium → kafka-clients pulls
    //   at.yawk.lz4:lz4-java (the maintained fork by the same author — same classes,
    //   same net.jpountz.lz4.* package). lz4-java declares the Gradle capability
    //   'net.jpountz.lz4:lz4', so Gradle sees two modules claiming the same capability
    //   and rejects both rather than auto-resolving. Excluding the abandoned artifact
    //   removes the conflict; lz4-java (already on the classpath via kafka-clients)
    //   satisfies mapdb's runtime need.
    implementation(project(":hmg-role-sdk")) {
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.squareup.okhttp3")
        exclude(group = "net.jpountz.lz4", module = "lz4")
    }

    // AWS IAM (SDK v2)
    implementation(platform("software.amazon.awssdk:bom:$awsSdkBomVersion"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:iam")

    // debezium: used in embedded mode (no Kafka broker, no connect-runtime).
    // Several of its modules transitively pull things we don't want:
    //
    // swagger-annotations: debezium depends on an older io.swagger.core.v3:swagger-annotations
    //   via kafka-connect. springdoc manages a newer version; having both on the classpath
    //   causes annotation scanning conflicts. Excluded from every debezium module that brings it.
    //
    // kafka connect-api / connect-file / connect-runtime: not needed for embedded debezium.
    //   connect-runtime alone drags in the full Jersey + HK2 servlet stack (Jetty, Jersey,
    //   GlassFish HK2), which conflicts with Spring Boot's embedded Tomcat.
    //
    // assertj: debezium test utilities bleed assertj into the compile classpath.
    //   We manage assertj explicitly in testImplementation at a known version below.
    implementation(platform("io.debezium:debezium-bom:$debeziumBomVersion"))

    implementation("io.debezium:debezium-api") {
        exclude(group = "io.swagger.core.v3", module = "swagger-annotations")
        exclude(group = "org.apache.kafka", module = "connect-api")
        exclude(group = "org.assertj")
    }
    implementation("io.debezium:debezium-embedded") {
        exclude(group = "io.swagger.core.v3", module = "swagger-annotations")
        exclude(group = "org.apache.kafka", module = "connect-file")
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.bitbucket.b_c")
        // connect-runtime pulls the full Jersey/HK2 stack — not needed for embedded debezium
        exclude(group = "org.glassfish.jersey.core")
        exclude(group = "org.glassfish.jersey.containers")
        exclude(group = "org.glassfish.jersey.inject")
        exclude(group = "org.glassfish.hk2")
        exclude(group = "org.glassfish.hk2.external")
        exclude(group = "org.assertj")
    }
    implementation("io.debezium:debezium-connector-mysql") {
        exclude(group = "io.swagger.core.v3", module = "swagger-annotations")
        exclude(group = "org.assertj")
    }

    testImplementation("org.assertj:assertj-core:$assertjVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    // to prevent errors "cannot open"
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/sun.reflect.annotation=ALL-UNNAMED")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    exclude("**/HmgRoleApiApplicationTests.class")
    exclude("**/ScopeMatcherTest.class")
    exclude("**/CucumberIntegrationTest.class")
    exclude("**/CucumberSpringConfiguration.class")
}
