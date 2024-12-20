plugins {
    id("cherryngine-micronaut-lib")
}

dependencies {
    api(project(":lib-math"))

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.guava)

    api(libs.minestom)

    api(libs.hollowcube.polar)


    implementation("org.jctools:jctools-core:4.0.3")
}