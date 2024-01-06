package workshop

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.nio.file.Files

abstract class FileLinePrefixingTask : DefaultTask() {
    //опишем наши inputs
    //RegularFileProperty - Этот тип проперти описывает, что у нас в проекте есть пропертя,
    //которая должна в момент экзекьюшена должна указывать на реальный файл.
    //На это поле сеттеры и геттеры делать не нужно, они автоматом идут
    //но нужна аннотация для input -> либо Internal либо InputFile
    @get: InputFile
    //@get: SkipWhenEmpty //пропустить, когда пустой input, т.е. ничего не делать
    @get: Incremental //SkipWhenEmpty уже включена в Incremental
    abstract val inputFile: RegularFileProperty

    //опишем наши outputs
    //На это поле сеттеры и геттеры делать не нужно, они автоматом идут
    //но нужна аннотация для output -> либо Internal либо OutputFile
    @get: OutputFile
    abstract val outputFile: RegularFileProperty

    //опишем наш экшен в open функции с аннотацией @TaskAction.
    //open - чтобы её могли другие разработчики кастомизировать под себя.
    //Т.к. в котлине все методы по умолчанию final.
    //В одном классе больше, чем один @TaskAction не делайте, т.к.
    //не гарантируется порядок их выполнения.
    //Чисто теоретически никто нам не запрещает делать несколько
    //экшенов, и настроить у них ордер через doFirst, doLast
    @TaskAction
    open fun prefixFileLines(changes: InputChanges) {


        //вычитываем содержимое файла, и добавляем каждой строке префикс 'prefix:'
        val output = inputFile.get().asFile //получаем File
            .readLines() //List<String> - читаем строки из файла
            .joinToString("\n") { "prefix: $it" } //добавили префикс каждой строке 'prefix:' и заджойнили через "\n"

        //с помощью проперти outputFile запишем полученные данные в файл
        //где физически будут находится файлы это пишется при регистрации таски в build.gradle.kts
        val outputFile = outputFile.get().asFile
        //создадим директорию, куда сохранять файл, т.к. без этого будет ошибка
        Files.createDirectories(outputFile.toPath().parent)
        outputFile.writeText(output)
    }
}