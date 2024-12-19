plugins {
    id("cherryngine-micronaut-lib")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.guava)

    api(libs.minestom)
}