package workshop

import org.gradle.api.JavaVersion
import org.gradle.api.provider.Property

/*
* Этот класс нужен, чтобы наш плагин смогли настраивать другие под себя.
* Т.е. выносим некоторые проперти наружу, которые через dsl можно настраивать.
* Это делается для того, чтобы не изменять ничего в самих тасках.
*
* Документация: https://docs.gradle.org/current/userguide/custom_plugins.html#sec:getting_input_from_the_build
*
* Посмотреть какая версия предлагается в вариантах после добавления extension в плагин JavaPlugin
* можно так:
* .\gradlew producer:outgoingVariants
*
*/
abstract class JavaPluginExtention {
    abstract val javaVersion: Property<Int>

    //обязательно проинициализируем версию
    init {
        javaVersion.set(11)
    }
}