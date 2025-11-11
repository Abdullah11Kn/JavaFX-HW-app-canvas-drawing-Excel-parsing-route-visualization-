plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi-ooxml:5.2.5")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("app.MainApp")
}

javafx {
    version = "21.0.1"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


