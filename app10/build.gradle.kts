// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // The Compose compiler is shipped with the Kotlin plugin since Kotlin 2.0, so it is
    // applied as a plugin instead of through the composeOptions block.
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
