package workshop

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.registering

/**
 * Палагины  - это очень простая концепция.
 * Плагин – это способ логики из build.gradle.kts (билдскрипт) вынести в отдельный класс.
 * Чаще всего пишут плагины для объекта Project.
 * Напишем плагин, который добавит таску из build.gradle.kts FileLinePrefixing3Task к нам в
 * проект автоматически.
 * Чтобы добавить этот плагин в build.gradle.kts мы просто добавим функцию
 * apply<FileLinePrefixingPlugin>()
 * и импорт
 * import workshop.FileLinePrefixingPlugin
 * Для запуска таски из плагина делаем как обычно:
 * .\gradlew fileLinePrefixing3Task --info
 */
class FileLinePrefixingPlugin : Plugin<Project> {
    /*Немного изменим и добавим ресивер с помощью with, который будет указывать на Project
    * И в последней строке лямбды указываем возвращаемое значение как Unit, т.к. метод
    * Plugin apply возвращет void.*/
    override fun apply(project: Project) = with(project) {
        //эта часть кода практически скопирована из build.gradle.kts и вставлена сюда с
        //небольшими изменениями. Сравни с таской в build.gradle.kts - fileLinePrefixing2Task
        //Регистрируем таску, задаем её имя, но правда через функцию - tasks.register
        tasks.register("fileLinePrefixing3Task", FileLinePrefixing3Task::class.java) {
            println("Configuration time $this")
            //Теперь, как и в таске в build.gradle.kts описываем input и
            //через метод set говорим откуда взять файл/файлы для вычитки, т.е. указываем директорию
            inputDir.set(file("filess"))
            //Теперь, как и в таске в build.gradle.kts описываем output и
            //через метод set говорим куда положить файл/файлы после преобразования, в какую директорию
            outputDir.set(file("$buildDir/filess"))
        }
        Unit
    }

//    override fun apply(target: Project) {
//        здесь идет имплементация метода
//    }
}