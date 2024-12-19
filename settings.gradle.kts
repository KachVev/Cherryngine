pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    includeBuild("build-logic")
}

rootProject.name = "Cherryngine"

include(
    "engine-core",

    "impl-demo"
)

includeBuild("lib-minestom") {
    dependencySubstitution {
        substitute(module("net.minestom:minestom-local")).using(project(":"))
        substitute(module("com.github.Minestom:Minestom")).using(project(":"))
    }
}