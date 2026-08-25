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

    // Akka framework for multi-tenancy
    implementation("com.typesafe.akka:akka-actor_2.13:2.6.20")
    implementation("com.typesafe.akka:akka-http_2.13:10.2.10")

    // JWT for authentication
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("redis.clients:jedis:4.4.3")

    // XML/JSON Processing
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.module:jackson-module-scala_2.13:2.15.3")
    implementation("com.google.code.gson:gson:2.10.1")

    // Utilities
    implementation("org.scala-lang.modules:scala-xml_2.13:2.1.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.13.0")
    implementation("log4j:log4j:1.2.17")

    // HTTP client
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.scalatest:scalatest_2.13:3.2.16")
}

tasks.named<JavaCompile>("compileJava") {
    enabled = false
}

java.sourceCompatibility = JavaVersion.VERSION_21

application {
    mainClass.set("com.webapi.Application")
}
