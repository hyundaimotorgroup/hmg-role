val lombokVersion = "1.18.32"
val mapstructVersion = "1.5.5.Final"
val jsr305Version = "3.0.2"
val mockitoBomVersion = "5.17.0"

allprojects {

    group = "com.hmg.role"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}

subprojects {

    plugins.withType<JavaPlugin> {
        dependencies {
            // Add Lombok for compile-time only code generation.
            "compileOnly"("org.projectlombok:lombok:$lombokVersion")
            "annotationProcessor"("org.projectlombok:lombok:$lombokVersion")

            "implementation"("org.mapstruct:mapstruct:$mapstructVersion")
            "annotationProcessor"("org.mapstruct:mapstruct-processor:$mapstructVersion")

            "compileOnly"("com.google.code.findbugs:jsr305:$jsr305Version")

            // Mockito 5.4+ is required for Java 21 support — Byte Buddy in 4.x only
            // supports up to Java 20 (class file version 64) and throws IllegalArgumentException
            // when trying to instrument Java 21 classes. mockito-inline was merged into
            // mockito-core in 5.0 and no longer exists as a separate artifact.
            "testImplementation"(platform("org.mockito:mockito-bom:$mockitoBomVersion"))
            "testImplementation"("org.mockito:mockito-core")
            "testImplementation"("org.mockito:mockito-junit-jupiter")
        }
    }

}

