plugins { kotlin("jvm"); kotlin("plugin.serialization"); application }
kotlin { jvmToolchain(17) }
application { mainClass.set("za.co.cttrains.workers.ServerKt") }
dependencies {
    implementation(project(":core"))
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.apache.pdfbox:pdfbox:3.0.7")
    testImplementation("junit:junit:4.13.2")
}

sourceSets.test { resources.srcDir(file("../../fixtures/providers/pdf")) }
