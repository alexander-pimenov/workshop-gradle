package workshop

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

/**
 * Этот плагин позволяет сказать, что такое runtime и что такое API.
 * Он экстендит JavaPlugin и расширяет его.
 *
 * Для примера можно посмотреть, как это сделано у Gradle
 * https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
 */
class JavaLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        //подключим сюда предыдущий плагин JavaPlugin, который мы хотим расширить
        pluginManager.apply(JavaPlugin::class.java)

        //добавим новую конфигурацию
        //эта конфигурация является dependency-backet
        //аналог конфигурации implementation
        val api = configurations.create("api"){
            //эта конфигурация не может быть потреблена
            isCanBeConsumed = false
            //эта конфигурация не может быть зарезолвлена
            isCanBeResolved = false
        }

        configurations.named("implementation"){
            //в конфигурацию implementation добавим то, что входит в api
            extendsFrom(api)
        }

        configurations.named("classpath"){
            //в конфигурацию classpath добавим то, что входит в api
            extendsFrom(api)
        }


        //в этой конфигурации мы хотим что бы наши библиотеки в Producer (и библиотека Guava) выходила наружу
        //при подключении Producer
        //тут предоставляем классы
        val apiClasses = configurations.create("apiClasses"){
            //эта конфигурация может быть потреблена
            isCanBeConsumed = true
            //эта конфигурация не может быть зарезолвлена
            isCanBeResolved = false
            //в эту конфигурацию должна попасть api и classes из класса LibraryPlugin
            extendsFrom(configurations.findByName("classes"), api)
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, JavaVersion.current().majorVersion.toInt())
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.CLASSES))
            }
        }

        //в этой конфигурации мы хотим что бы наши библиотеки в Producer (и библиотека Guava) выходила наружу
        //при подключении Producer
        //тут предоставляем элементы
        val apiElements = configurations.create("apiElements"){
            //эта конфигурация может быть потреблена
            isCanBeConsumed = true
            //эта конфигурация не может быть зарезолвлена
            isCanBeResolved = false
            //в эту конфигурацию должна попасть api
            extendsFrom(api)
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, JavaVersion.current().majorVersion.toInt())
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            }
        }

        //добавим артефакты в наши outgoingVariants
        artifacts {
            //в apiClasses уже ничего добавлять не нужно, т.к. в apiClasses уже классы добавили через extendsFrom

            //добавим артефакты в apiElements
            //просто указываем таску jar через поиск таски по findByName("jar")
            add(apiElements.name, tasks.findByName("jar") as Jar)
        }

        //добавим компонент, чтобы можно было публиковать артефакты
        (components.findByName("java") as AdhocComponentWithVariants).apply {
            //Добавим в javaComponent outgoingVariants из классов apiClasses
            addVariantsFromConfiguration(apiClasses){
                //Тут мы его объявляем, но не хотим публиковать в мавен
                //Поэтому скипаем его.
                skip()
            }

            //Добавим в javaComponent outgoingVariants из конфигурации apiElements
            addVariantsFromConfiguration(apiElements){
                //и хотим чтобы apiElements мапались на мавен скоуп compile
                mapToMavenScope("compile")
            }
            components.add(this)
        }




        Unit
    }
}