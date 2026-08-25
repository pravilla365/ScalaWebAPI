plugins {
    id("scala")
    id("java")
    id("application")
}

group = "com.webapi"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.12")

    // XML Processing
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.1")

    // JSON Processing
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.module:jackson-module-scala_2.13:2.16.1")

    // HTTP client
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.scalatest:scalatest_2.13:3.2.17")
}

tasks.named<JavaCompile>("compileJava") {
    enabled = false
}

java.sourceCompatibility = JavaVersion.VERSION_21

application {
    mainClass.set("com.webapi.Application")
}
