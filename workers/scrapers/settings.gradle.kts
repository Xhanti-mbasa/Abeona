pluginManagement { repositories { mavenCentral(); gradlePluginPortal() };plugins { kotlin("jvm") version "2.1.20";kotlin("plugin.serialization") version "2.1.20" } }
dependencyResolutionManagement { repositories { mavenCentral() } }
rootProject.name="scrapers"
include(":core")
project(":core").projectDir=file("../../packages/transit-core")
