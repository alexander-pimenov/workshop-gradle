package workshop

import org.gradle.api.JavaVersion
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

/*Удобная функция, чтобы инлайнить проперти.
* Она позволяет вычислять дженерик исходя из сигнатуры функции
* Т.е. в том месте, где для указания аттрибутов используется objects.named,
* можно использвать её.*/
inline fun <reified T : Named> Project.nameAttribute(value: String) = objects.named(T::class.java, value)

class JavaPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        //что бы использовать другие библиотеки, например, Guava, нужно
        //научиться объявлять зависимости.
        //конфигурация зависимостей, которая позволит нам указать координаты каких то артефактов
        //которые мы будем использовать у себя в билде (локальные, удаленные)
        //например, для Guava имеем такие координаты: "com.google.guava:guava:33.0.0-jre"

        // предоставляем нашу внутреннюю конфигурацию
        // тут мы просто объявляем бакет зависимость,
        // т.е. мы не хотим не резолвить эту конфигурацию (isCanBeResolved=false)
        // и не хотим её никому предоставлять (isCanBeConsumed=false)
        val implementation = configurations.create("implementation") {
            //в конфигурации важно описать два поля, для чего эта конфигурация используется
            //рекомендуется заполнять эти два поля:
            //(isCanBeConsumed=true т.к. мы хотим получать то что положили поэтому true)
            isCanBeConsumed = false
            //(isCanBeResolved=false т.к. мы не хотим её резолвить, т.е. мы экстендим другую конфигурацию и
            //должны потом сформировать один финальный граф-файл, но FASLE говорит, что мы не делаем это,
            //а просто отдаем наружу)
            //Еще пример(обратный): например когда нужен runtime classpash то нужно резолвить кофигурацию, т.е.
            //будет isCanBeConsumed=false и isCanBeResolved=true
            isCanBeResolved = false

            //с помощью extendsFrom можно сюда заэкстендить любую другую конфигурацию,
            //т.е. переиспользовать уже какую-то готовую конфигурацию
            //но здесь это больше для примера, поэтому закоментировано
            //extendsFrom()
        }

        //
        val runtimeClasspath = configurations.create("runtimeClasspath"){
            isCanBeConsumed = false
            isCanBeResolved = true
            //всё что есть в implementation, должно попасть в runtimeClasspath
            extendsFrom(implementation)
        }


        //регистрируем таску для компиляции
        //таску сконфигурировали в самой таске в блоке init{},
        // а здесь в блоке {} сетим classpath
        val compileJava = tasks.register("compile", CompileJavaTask::class.java) {
            //передаем в classpath файлы из runtimeClasspath
            //т.е. всё что влили в implementation попадет и в runtimeClasspath
            //и попадает сюда в classPath
            classPath.setFrom(runtimeClasspath)
        }

        //регистрируем таску для jar
        //jar нужны при запуске приложения
        //Используем уже готовую таску Jar, которая умеет создавать jar
        val jar = tasks.register("jar", Jar::class.java) {
            //куда положить jar эта таска сама знает поэтому ничего не нужно задавать

            //окуда брать файлы для jar нужно для таски указать.
            //забираем мы от туда куда скомпилирует наша таска compileJava
            //вызвать эту таску для producer можем так:
            //".\gradlew producer:jar"
            from(compileJava.map { it.outputDir })
        }


        //предоставим Variant конфигурации для скомпилированных классов classes
        val classes = configurations.create("classes") {
            //в конфигурации важно описать два поля, для чего эта конфигурация используется
            //рекомендуется заполнять эти два поля:
            //(isCanBeConsumed=true т.к. мы хотим получать то что положили поэтому true)
            isCanBeConsumed = true
            //(isCanBeResolved=false т.к. мы не хотим её резолвить, т.е. мы экстендим другую конфигурацию и
            //должны потом сформировать один финальный граф-файл, но FASLE говорит, что мы не делаем это,
            //а просто отдаем наружу)
            //Еще пример(обратный): например когда нужен runtime classpash то нужно резолвить кофигурацию, т.е.
            //будет isCanBeConsumed=false и isCanBeResolved=true
            isCanBeResolved = false

            //с помощью extendsFrom можно сюда заэкстендить любую другую конфигурацию,
            //т.е. переиспользовать уже какую-то готовую конфигурацию
            //здесь всё что входит в конфигурацию implementation автоматом попадает в runtimeElements.
            // extendsFrom(implementation)

            //Что бы понять как правильно сделать нам нужно понимать, что от нас ждет Consumer.
            //Есть команда
            //".\gradlew consumer:outgoingVariants"
            //которая показывает какие артефакты предоставляет проект Consumer.
            //Например, там есть список атрибутов:
            //       - Attributes
            //          - org.gradle.category            = library
            //          - org.gradle.dependency.bundling = external
            //          - org.gradle.jvm.version         = 11
            //          - org.gradle.libraryelements     = resources
            //          - org.gradle.usage               = java-runtime

            //Укажем аттрибуты для нашего проекта
            // (они дефолтные и взяты из https://docs.gradle.org/current/userguide/cross_project_publications.html#ex-declaring-the-variant-attributes)
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, JavaVersion.current().majorVersion.toInt())
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.CLASSES))
            }
        }

        //предоставим Variant конфигурации для jar во время выполнения (runtimeElements)
        val runtimeElements = configurations.create("runtimeElements") {
            //в конфигурации важно описать два поля, для чего эта конфигурация используется
            //рекомендуется заполнять эти два поля:
            //(isCanBeConsumed=true т.к. мы хотим получать то что положили поэтому true)
            isCanBeConsumed = true
            //(isCanBeResolved=false т.к. мы не хотим её резолвить, т.е. мы экстендим другую конфигурацию и
            //должны потом сформировать один финальный граф-файл, но FASLE говорит, что мы не делаем это,
            //а просто отдаем наружу)
            //Еще пример(обратный): например когда нужен runtime classpash то нужно резолвить кофигурацию, т.е.
            //будет isCanBeConsumed=false и isCanBeResolved=true
            isCanBeResolved = false

            //с помощью extendsFrom можно сюда заэкстендить любую другую конфигурацию,
            //т.е. переиспользовать уже какую-то готовую конфигурацию
            //здесь всё что входит в конфигурацию implementation автоматом попадает в runtimeElements.
            // extendsFrom(implementation)

            //зададим проброс библиотек из конфигурации implementation, например той же Guava, которую используем для примера,
            //т.е. всё что влили в implementation попадет и сюда в runtimeElements
            //это еще называется - транзитивные зависимости.
            extendsFrom(implementation)

            //Что бы понять как правильно сделать нам нужно понимать, что от нас ждет Consumer.
            //Есть команда
            //".\gradlew consumer:outgoingVariants"
            //которая показывает какие артефакты предоставляет проект Consumer.
            //Например, там есть список атрибутов:
            //       - Attributes
            //          - org.gradle.category            = library
            //          - org.gradle.dependency.bundling = external
            //          - org.gradle.jvm.version         = 11
            //          - org.gradle.libraryelements     = resources
            //          - org.gradle.usage               = java-runtime

            //Укажем аттрибуты для нашего проекта
            // (они дефолтные и взяты из https://docs.gradle.org/current/userguide/cross_project_publications.html#ex-declaring-the-variant-attributes)
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, JavaVersion.current().majorVersion.toInt())
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            }
        }

        //добавим артефакты
        artifacts {
            //передадим между нашими подпроектами Producer и Consumer только скомпилирвованные классы
            //для этого используем диреторию нашей таски. Но у нас есть Provider и мы из
            //него возьмем директорию.
            //Способ описания зависимостей через Provider позволяет нам автоматически связывать наши объекты.
            //Здесь мы связываем конфигурацию с выполнением таски.
            //Т.е. если кто-то попросит из-вне эту конфигурацию, мы у себя выполним таску,
            // которая производит результаты этой конфигурации.
            add(classes.name, compileJava.map {
                it.outputDir.get().asFile
            })

            //добавим артефакты в runtimeElements
            //просто указываем таску jar без функции map{} как было для скомпилированных классов
            add(runtimeElements.name, jar)
        }


        //ВНИМАНИЕ: это вариант легаси, но устарел, так не нужно делать
//        configurations.named("default") {
//            extendsFrom(runtimeElements)
//        }

        Unit
    }
}