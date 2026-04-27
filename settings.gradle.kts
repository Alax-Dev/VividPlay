pluginManagement {
 repositories {
  google {
   content {
    includeGroupByRegex("com\\.android.*")
    includeGroupByRegex("com\\.google.*")
    includeGroupByRegex("androidx.*")
   }
  }
  mavenCentral()
  gradlePluginPortal()
  // Add Kotlin plugin repository explicitly
  maven { url = uri("https://plugins.gradle.org/m2/") }
 }
}

dependencyResolutionManagement {
 repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
 repositories {
  google()
  mavenCentral()
 }
}

rootProject.name = "VividPlay"
include(":app")
