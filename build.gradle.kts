plugins {
    id("java")
}

group = "org.beatrice"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    runtimeOnly("org.postgresql:postgresql:42.7.7")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
    testImplementation("org.slf4j:slf4j-nop:2.0.12")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.18.0")
    mockitoAgent("org.mockito:mockito-core:5.18.0") { isTransitive = false }
}

tasks.test {
    jvmArgs = (jvmArgs ?: mutableListOf()).apply {
        add("-javaagent:${mockitoAgent.asPath}")
    }
    useJUnitPlatform()
}
