plugins {
    kotlin("jvm") version "1.9.10" apply false
    id("com.android.application") version "8.2.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
