import java.time.Duration

/*
 * FreeMind CE - Main Module Build Configuration
 * Denomas - 2026
 *
 * Modern Java 21 + Gradle + JAXB build system
 */

plugins {
    java
    application
    jacoco
    pmd
    id("com.github.spotbugs") version "6.5.1"
    id("org.owasp.dependencycheck") version "12.2.1"
    id("org.cyclonedx.bom") version "2.3.1"
    id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.denomas.freemind"
version = "1.3.4"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

// ============================================================================
// Java Configuration
// ============================================================================

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

// Generate version.properties from the project version
val generateVersionProperties by tasks.registering {
    description = "Generates version.properties with the current project version"
    group = "Generation"
    val outputDir = layout.buildDirectory.dir("generated-resources")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "version.properties").writeText("freemind.version=${project.version}\n")
    }
}

tasks.processResources {
    dependsOn(generateVersionProperties)
    from(generateVersionProperties.map { it.outputs.files })
    filteringCharset = "UTF-8"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

application {
    // The Gradle project name is bespoke-freemind-ce. The install directory stays freemind.
    applicationName = "freemind"
    mainClass.set("freemind.main.FreeMindStarter")
    applicationDefaultJvmArgs = listOf(
        "-Xms64m",
        "-Xmx512m",
        "-Xss8M",
        "-Dapple.laf.useScreenMenuBar=true",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

// ============================================================================
// Version Catalog (Centralized Dependency Management)
// ============================================================================

// jpackage requires numeric-only version (strips any suffix like -SNAPSHOT)
val jpackageVersion = project.version.toString().replace(Regex("-.*"), "")

val jaxbApiVersion = "2.3.1"
val jaxbImplVersion = "2.3.9"
val batikVersion = "1.19"
val fopVersion = "2.11"
val flatlafVersion = "3.7.1"
val jgoodiesFormsVersion = "1.9.0"
val jgoodiesCommonVersion = "1.8.1"
val junitVersion = "4.13.2"

// ============================================================================
// Dependencies
// ============================================================================

// Separate configuration for XJC code generation tool
val xjc by configurations.creating

dependencies {
    // JAXB (XML Binding) - replaces JiBX
    implementation("javax.xml.bind:jaxb-api:${jaxbApiVersion}")
    implementation("org.glassfish.jaxb:jaxb-runtime:${jaxbImplVersion}")

    // Activation (required for JAXB on JDK 11+)
    implementation("javax.activation:activation:1.1.1")

    // XJC tool (code generation only - not needed at runtime)
    xjc("org.glassfish.jaxb:jaxb-xjc:${jaxbImplVersion}")
    xjc("org.glassfish.jaxb:jaxb-runtime:${jaxbImplVersion}")

    // Look and Feel - FlatLaf (Modern, Cross-Platform)
    implementation("com.formdev:flatlaf:${flatlafVersion}")
    implementation("com.formdev:flatlaf-extras:${flatlafVersion}")

    // JGoodies (Forms and Look) - 1.8.0 has setDefaultDialogBorder API
    implementation("com.jgoodies:jgoodies-forms:${jgoodiesFormsVersion}")
    implementation("com.jgoodies:jgoodies-common:${jgoodiesCommonVersion}")

    // SimplyHTML has no matching artifact on Maven Central.
    implementation(files("lib/SimplyHTML/SimplyHTML.jar"))
    // The note editor calls gnu.regexp.RE when it sets text.
    implementation("gnu-regexp:gnu-regexp:1.1.4")

    // Apache Xalan (XSLT Processing)
    implementation("xalan:xalan:2.7.3")
    implementation("xalan:serializer:2.7.3")
    implementation("xml-apis:xml-apis:2.0.2")
    implementation("xerces:xercesImpl:2.12.2")

    // Jsoup (HTML Parsing)
    implementation("org.jsoup:jsoup:1.22.2")

    // Leftover coordinates stay until the trimmed stage
    implementation("org.codehaus.groovy:groovy-all:3.0.25")
    implementation("org.apache.lucene:lucene-core:10.4.0")
    implementation("org.apache.lucene:lucene-analysis-common:10.4.0")
    implementation("org.apache.lucene:lucene-queryparser:10.4.0")
    // JLaTeXMath stays until the trimmed stage
    implementation("org.scilab.forge:jlatexmath:1.0.7")

    // Batik stays until the trimmed stage
    implementation("org.apache.xmlgraphics:batik-all:${batikVersion}")

    // FOP stays until the trimmed stage
    implementation("org.apache.xmlgraphics:fop:${fopVersion}")

    // Logging (SLF4J + Logback)
    implementation("org.slf4j:slf4j-api:2.0.20")
    implementation("ch.qos.logback:logback-classic:1.6.5")

    // Testing - JUnit 5 with vintage engine for JUnit 3 backward compatibility
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.junit.vintage:junit-vintage-engine:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
    testImplementation("junit:junit:${junitVersion}")
    testImplementation("org.mockito:mockito-core:5.24.0")

    // Property-Based Testing (jqwik)
    testImplementation("net.jqwik:jqwik:1.10.1")
    testImplementation("net.jqwik:jqwik-engine:1.10.1")

    // Fuzz Testing (Jazzer via JUnit integration)
    testImplementation("com.code-intelligence:jazzer-api:0.30.0")
    testImplementation("com.code-intelligence:jazzer-junit:0.30.0")

    // Fluent Assertions (AssertJ)
    testImplementation("org.assertj:assertj-core:3.27.7")

    // AssertJ Swing (GUI testing with screenshots)
    testImplementation("org.assertj:assertj-swing-junit:3.17.1")
}

// ============================================================================
// Source Sets
// ============================================================================

// Defaults are src/main/java, src/main/resources, src/test/java, src/test/resources.
// JAXB output is an extra main source directory and is not committed.
val generatedJava = layout.buildDirectory.dir("generated/sources/jaxb/java")

sourceSets {
    main {
        java {
            srcDir(generatedJava)
            // JApplet and SecurityManager are gone from the JDK this tree compiles with.
            exclude("**/FreeMindApplet.java")
            exclude("**/FreeMindSecurityManager.java")
        }
    }
    test {
        java {
            // AllTests is a JUnit 3 suite. Vintage would run it and the classes again.
            exclude("**/AllTests.java")
        }
    }
}

// ============================================================================
// JAXB Code Generation Task
// ============================================================================

tasks.register<JavaExec>("generateJaxb") {
    description = "Generates JAXB annotated classes from XSD using xjc"
    group = "Generation"

    val schema = layout.projectDirectory.file("src/main/schema/freemind_actions.xsd")
    val compatPatch = layout.projectDirectory.file("src/main/schema/jaxb-jibx-compat.patch")
    inputs.file(schema)
    inputs.file(compatPatch)
    outputs.dir(generatedJava)

    classpath = configurations["xjc"]
    mainClass.set("com.sun.tools.xjc.XJCFacade")

    doFirst {
        val dir = generatedJava.get().asFile
        delete(dir)
        dir.mkdirs()
    }

    args = listOf(
        "-d", generatedJava.get().asFile.absolutePath,
        "-p", "freemind.controller.actions.generated.instance",
        "-extension",
        "-encoding", "UTF-8",
        "-no-header",
        schema.asFile.absolutePath
    )

    doLast {
        val root = generatedJava.get().asFile
        root.walkTopDown().filter { it.isFile && it.extension == "java" }.forEach { file ->
            val normalized = file.readLines().joinToString("\n") { it.trimEnd() } + "\n"
            file.writeText(normalized)
        }
        // xjc does not emit the JiBX names the rest of the program calls.
        val proc = ProcessBuilder(
            "patch", "-p1", "--forward", "--batch",
            "-d", root.absolutePath,
            "-i", compatPatch.asFile.absolutePath
        ).redirectErrorStream(true).start()
        val output = proc.inputStream.bufferedReader().readText()
        if (proc.waitFor() != 0) {
            error(output)
        }
    }
}

tasks.compileJava {
    dependsOn("generateJaxb")
}

// ============================================================================
// JAR Configuration
// ============================================================================

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("freemind-ce")
    manifest {
        attributes(
            "Main-Class" to "freemind.main.FreeMindStarter",
            "Built-By" to "Denomas",
            "Implementation-Title" to "FreeMind Classic Edition",
            "Implementation-Version" to project.version,
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }

}

// ============================================================================
// Distribution Configuration
// ============================================================================

tasks.distZip {
    archiveBaseName.set("freemind-ce")
}

tasks.distTar {
    archiveBaseName.set("freemind-ce")
}

// ============================================================================
// Custom Tasks for macOS Support
// ============================================================================

tasks.register<Copy>("prepareMacDist") {
    description = "Prepares distribution for macOS with proper key mappings"
    group = "Distribution"

    from("src/main/resources/freemind.properties")
    into("build/distributions/mac")

    filter { line ->
        line.replace(Regex("(?i)^keystroke(.*)=(.*)\\bcontrol\\b"), "keystroke\$1=\$2meta")
            .replace(Regex("(?i)^keystroke(.*)=(.*)\\binsert\\b"), "keystroke\$1=\$2TAB")
    }
}

// ============================================================================
// Test Configuration
// ============================================================================

tasks.test {
    useJUnitPlatform {
        excludeTags("gui", "fuzz")
    }
    // jqwik does not publish the JUnit tag, so the class patterns exclude it too.
    filter {
        excludeTestsMatching("tests.freemind.property.*")
        excludeTestsMatching("tests.freemind.unicode.UnicodePropertyTests")
        excludeTestsMatching("tests.freemind.fuzz.*")
    }

    // Headless mode for CI compatibility (Swing tests throw HeadlessException without this)
    systemProperty("java.awt.headless", "true")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<Test>("testGui") {
    description = "Runs GUI tests with screenshot capture (requires display)"
    group = "Verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    useJUnitPlatform {
        includeTags("gui")
    }

    // GUI tests need a real display — not headless
    systemProperty("java.awt.headless", "false")

    // Always re-run GUI tests — never use Gradle cache.
    // GUI tests depend on platform-specific rendering (fonts, display server)
    // so cached results from another runner or Java version are not reliable.
    outputs.upToDateWhen { false }

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<Test>("testPerformance") {
    description = "Run performance tests (large file, timing)"
    group = "verification"
    // A new Test task does not use the test source set unless told. Without
    // this, Gradle reports NO-SOURCE and the tagged tests never run.
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("performance")
    }
    jvmArgs("-Xmx512m")
    systemProperty("java.awt.headless", "true")
    maxHeapSize = "512m"
    timeout.set(Duration.ofMinutes(10))
}

tasks.register<Test>("testFuzz") {
    description = "Run jqwik property tests and Jazzer fuzz tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("tests.freemind.property.*")
        includeTestsMatching("tests.freemind.unicode.UnicodePropertyTests")
        includeTestsMatching("tests.freemind.fuzz.*")
    }
    systemProperty("java.awt.headless", "true")
    timeout.set(Duration.ofMinutes(30))
}

tasks.register<JavaExec>("showcaseScreenshots") {
    description = "Launches FreeMind CE with showcase mindmaps and captures full-screen desktop screenshots"
    group = "Verification"

    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("tests.freemind.gui.ShowcaseScreenshotCapture")
    systemProperty("java.awt.headless", "false")

    // Run from project root so relative paths to .mm files work
    workingDir = rootProject.projectDir

    // Always re-run
    outputs.upToDateWhen { false }
}

// ============================================================================
// Code Coverage (JaCoCo)
// ============================================================================

// JAXB classes are generated under the build directory. The report and the agent skip that package.
// Phase C does not add tests for those classes.
val generatedJaxbExclude = "freemind/controller/actions/generated/**"

fun org.gradle.api.file.FileCollection.withoutGeneratedJaxb() =
    files(map { dir ->
        fileTree(dir) {
            exclude(generatedJaxbExclude)
        }
    })

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        excludes = listOf("freemind.controller.actions.generated.instance.*")
    }
}

tasks.named<Test>("testFuzz") {
    configure<JacocoTaskExtension> {
        isEnabled = false
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("testGui"), tasks.named("testPerformance"))
    // testGui and testPerformance write their own exec files.
    // The default report only reads test.exec, so a full run would under-count.
    // testFuzz is outside this report. A leftover testChaos exec is too.
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")) {
        include("*.exec")
        exclude("testFuzz.exec", "testChaos.exec")
    })
    classDirectories.setFrom(classDirectories.withoutGeneratedJaxb())
    sourceDirectories.setFrom(sourceDirectories.withoutGeneratedJaxb())
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Mutation uses the unit tests. Reasons for the excludes are in docs/testspec.md.
fun csvGradleProperty(name: String, default: Set<String>): Set<String> {
    val raw = findProperty(name) as String?
    if (raw.isNullOrBlank()) {
        return default
    }
    return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}

pitest {
    pitestVersion.set("1.30.1")
    junit5PluginVersion.set("1.2.3")
    targetClasses.set(csvGradleProperty("mutateClasses", setOf("freemind.controller.filter.condition.*")))
    targetTests.set(csvGradleProperty("mutateTests", setOf(
        "tests.freemind.filter.*",
        "tests.freemind.ConditionFactoryBugFixTest"
    )))
    excludedClasses.set(setOf("freemind.controller.actions.generated.instance.*"))
    excludedTestClasses.set(setOf(
        "tests.freemind.fuzz.*",
        "tests.freemind.property.*",
        "tests.freemind.unicode.UnicodePropertyTests",
        "tests.freemind.gui.*",
        "tests.freemind.io.LargeFilePerformanceTest"
    ))
    excludedGroups.set(setOf("gui", "fuzz", "performance"))
    threads.set(2)
    mutators.set(setOf("DEFAULTS"))
    outputFormats.set(setOf("XML", "HTML"))
    timestampedReports.set(false)
    timeoutFactor.set("2".toBigDecimal())
    timeoutConstInMillis.set(10000)
    jvmArgs.set(listOf("-Djava.awt.headless=true", "-Xmx512m"))
    failWhenNoMutations.set(true)
    verbosity.set("DEFAULT")
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    // Same exec files as jacocoTestReport: unit, GUI, and performance.
    // testFuzz and a leftover testChaos exec stay out.
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")) {
        include("*.exec")
        exclude("testFuzz.exec", "testChaos.exec")
    })
    classDirectories.setFrom(classDirectories.withoutGeneratedJaxb())
    sourceDirectories.setFrom(sourceDirectories.withoutGeneratedJaxb())
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

// ============================================================================
// Static Analysis (PMD)
// ============================================================================

pmd {
    isConsoleOutput = true
    toolVersion = "7.21.0"
    rulesMinimumPriority.set(5)
    // Legacy codebase: don't fail build, report only.
    // New code should have zero PMD violations.
    isIgnoreFailures = true
}

tasks.withType<Pmd>().configureEach {
    ruleSetFiles = files("config/pmd/ruleset.xml")
    ruleSets = listOf()
}

// ============================================================================
// Static Analysis (SpotBugs)
// ============================================================================

spotbugs {
    ignoreFailures.set(false)
    reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
    excludeFilter.set(file("config/spotbugs/exclude.xml"))
    showProgress.set(false)
}

// Enable HTML reports for all SpotBugs tasks
afterEvaluate {
    tasks.withType(com.github.spotbugs.snom.SpotBugsTask::class.java).configureEach {
        reports.maybeCreate("html").apply {
            required.set(true)
        }
    }
}

// SpotBugs scans the main sourceSet classpath which includes plugin modules.
// The plugins directory is part of main sourceSet, so Gradle sees plugin outputs
// as implicit inputs to SpotBugs. Declare explicit dependencies on all plugin
// tasks that produce output in the shared directory to avoid validation errors.
tasks.matching { it.name.startsWith("spotbugs") }.configureEach {
    rootProject.subprojects.filter { it.path.startsWith(":freemind:plugins:") }.forEach { plugin ->
        plugin.tasks.matching {
            it.name in setOf(
                "compileJava", "compileTestJava", "processResources",
                "processTestResources", "classes", "testClasses", "jar", "test"
            )
        }.all {
            this@configureEach.dependsOn(this)
        }
    }
}

// ============================================================================
// Dependency Vulnerability Scanning (OWASP)
// ============================================================================

dependencyCheck {
    failBuildOnCVSS = 7f  // block on High+ severity CVEs
    suppressionFile = "config/dependency-check/suppressions.xml"
    analyzers.assemblyEnabled = false
    failOnError = true
    analyzers.retirejs.enabled = false
    analyzers.nodeAudit.enabled = false
    analyzers.nodeEnabled = false
}

// ============================================================================
// JPackage Task (Modern Java Packaging)
// ============================================================================

// Copy doc/ into jpackage input directory (after installDist, before jpackage)
// jpackage --input maps lib/ → app/ in installed app.
// getFreemindBaseDir() resolves to <install>/app/ for jpackage'd apps.
// So doc/ must be inside lib/doc/ → becomes app/doc/ after installation.
val copyDocForJpackage by tasks.registering(Copy::class) {
    description = "Copies doc/ folder into jpackage input directory"
    group = "Distribution"
    dependsOn(tasks.installDist)
    from("src/main/resources/doc")
    into("build/install/freemind/lib/doc")
    doLast {
        require(file("build/install/freemind/lib/doc/freemind.mm").exists()) {
            "doc/freemind.mm not found in jpackage input"
        }
    }
}

tasks.register<Exec>("jpackageMac") {
    description = "Creates macOS .dmg package"
    group = "Distribution"
    dependsOn(copyDocForJpackage)
    doFirst { mkdir("build/jpackage") }

    commandLine(
        "jpackage",
        "--type", "dmg",
        "--dest", "build/jpackage",
        "--name", "FreeMind-CE",
        "--input", "build/install/freemind/lib",
        "--main-jar", "freemind-ce-${project.version}.jar",
        "--main-class", "freemind.main.FreeMindStarter",
        "--icon", "src/main/resources/images/FreeMindWindowIconModern.icns",
        "--app-version", jpackageVersion,
        "--vendor", "Denomas",
        "--file-associations", "jpackage/file-associations.properties",
        "--java-options", "-Xms64m",
        "--java-options", "-Xmx512m",
        "--java-options", "-Xss8M",
        "--java-options", "-Dapple.laf.useScreenMenuBar=true",
        "--java-options", "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

tasks.register<Exec>("jpackageWin") {
    description = "Creates Windows .exe package"
    group = "Distribution"
    dependsOn(copyDocForJpackage)
    doFirst { mkdir("build/jpackage") }

    commandLine(
        "jpackage",
        "--type", "exe",
        "--dest", "build/jpackage",
        "--name", "FreeMind-CE",
        "--input", "build/install/freemind/lib",
        "--main-jar", "freemind-ce-${project.version}.jar",
        "--main-class", "freemind.main.FreeMindStarter",
        "--icon", "src/main/resources/images/FreeMindWindowIcon.ico",
        "--app-version", jpackageVersion,
        "--vendor", "Denomas",
        "--file-associations", "jpackage/file-associations.properties",
        "--win-shortcut",
        "--win-menu",
        "--win-menu-group", "FreeMind CE",
        "--java-options", "-Xms64m",
        "--java-options", "-Xmx512m",
        "--java-options", "-Xss8M",
        "--java-options", "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

tasks.register<Exec>("jpackageLinux") {
    description = "Creates Linux .deb package"
    group = "Distribution"
    dependsOn(copyDocForJpackage)
    doFirst { mkdir("build/jpackage") }

    commandLine(
        "jpackage",
        "--type", "deb",
        "--dest", "build/jpackage",
        "--name", "freemind-ce",
        "--input", "build/install/freemind/lib",
        "--main-jar", "freemind-ce-${project.version}.jar",
        "--main-class", "freemind.main.FreeMindStarter",
        "--icon", "src/main/resources/images/FreeMindWindowIcon.png",
        "--app-version", jpackageVersion,
        "--vendor", "Denomas",
        "--file-associations", "jpackage/file-associations.properties",
        "--java-options", "-Xms64m",
        "--java-options", "-Xmx512m",
        "--java-options", "-Xss8M",
        "--java-options", "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

tasks.register<Exec>("jpackageLinuxRpm") {
    description = "Creates Linux .rpm package"
    group = "Distribution"
    dependsOn(copyDocForJpackage)
    doFirst { mkdir("build/jpackage") }

    commandLine(
        "jpackage",
        "--type", "rpm",
        "--dest", "build/jpackage",
        "--name", "freemind-ce",
        "--input", "build/install/freemind/lib",
        "--main-jar", "freemind-ce-${project.version}.jar",
        "--main-class", "freemind.main.FreeMindStarter",
        "--icon", "src/main/resources/images/FreeMindWindowIcon.png",
        "--app-version", jpackageVersion,
        "--vendor", "Denomas",
        "--file-associations", "jpackage/file-associations.properties",
        "--java-options", "-Xms64m",
        "--java-options", "-Xmx512m",
        "--java-options", "-Xss8M",
        "--java-options", "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

tasks.register<Exec>("jpackageWinMsi") {
    description = "Creates Windows .msi installer package"
    group = "Distribution"
    dependsOn(copyDocForJpackage)
    doFirst { mkdir("build/jpackage") }

    commandLine(
        "jpackage",
        "--type", "msi",
        "--dest", "build/jpackage",
        "--name", "FreeMind-CE",
        "--input", "build/install/freemind/lib",
        "--main-jar", "freemind-ce-${project.version}.jar",
        "--main-class", "freemind.main.FreeMindStarter",
        "--icon", "src/main/resources/images/FreeMindWindowIcon.ico",
        "--app-version", jpackageVersion,
        "--vendor", "Denomas",
        "--file-associations", "jpackage/file-associations.properties",
        "--win-per-user-install",
        "--win-shortcut",
        "--win-menu",
        "--win-menu-group", "FreeMind CE",
        "--win-shortcut-prompt",
        "--java-options", "-Xms64m",
        "--java-options", "-Xmx512m",
        "--java-options", "-Xss8M",
        "--java-options", "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--java-options", "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

// ============================================================================
// Documentation Tasks
// ============================================================================

tasks.javadoc {
    source = sourceSets["main"].allJava
    classpath = sourceSets["main"].runtimeClasspath
    destinationDir = file("build/docs/javadoc")

    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addStringOption("Xdoclint:all,-missing", "-quiet")
    }
}
