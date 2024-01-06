package workshop

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

/*
* На механизме инкрементальной сборки рассмотренной в кастомной таске ниже, строятся
* билды, которые позволяют нам не клинить директорию и теперь можно собрить наш проект
* переиспользуя инпуты и отпуты от предыдущих запусков.
* Можно сравнивать, что было на предыдущем запуске и соответственно
* на следующем запуске часть из этого переиспользовать.
* Это очень полезно на больших объемах файлов, например нужно скомпилировать 1000 классов,
* и если делать `gradle clean build` то каждый раз будет перекомпилироваться всё.
*
* Т.е. если мы пометим инпут и отпут как @get:InputDirectory и @get:OutputDirectory
* т не делать `clean`, то результат билда можно переиспользовать.
* И сама таска может выполняться инкрементально, т.е. если были изменениято она выполнится.
* А раньше таска выполнялась или не выполнялась целиком.
*
* Если мы пометим нашу таску как @CacheableTask, то это мы говорим, что её не плохо бы
* кэшировать при её исполнении.
* Для запуска с кэшом нужно добавлять ключ --build-cache, т.к. по умолчанию он отключен.
* Т.е. например мы делаем `gradle clean`, потом запускаем таску
* .\gradlew fileLinePrefixing2Task --build-cache --info
* Получим сообщение: Stored cache entry for task ':fileLinePrefixing2Task' with cache key 296275b593cb1be1e813392d6bba6aca
* Потом удалим директорию `build` и снова запустим
* .\gradlew fileLinePrefixing2Task --build-cache --info
* то получим сообщение около таски:
* "> Task :fileLinePrefixing2Task FROM-CACHE"
* что говорит о том, что результат таски взяли из кэша, а не сделали реальное выполнение.
* Сериализованный результат таски в Кэше для Gradle находится в "C:\Users\pimal\.gradle\caches\build-cache-1"
* build-cache-1 - это билд таски, т.е. output таски утаскивается в кэш.
* Во время билда инпуты каждой таски, если она @CacheableTask, кэшируются и outputs преобразуются
* в бинарный контент и сохраняются в папке build-cache-1.
* Потом при выполнении таски её input/output может забираться из кэша.
* В крупных компаниях кэш может делаться удаленно, что бы все разработчики имели к нему
* доступ, а не локально собирали по 100 модулей проекта.
*
* */
//https://docs.gradle.org/current/userguide/custom_tasks.html#incremental_tasks
@CacheableTask
abstract class FileLinePrefixing2Task : DefaultTask() {

    @get: Incremental
    @get: PathSensitive(PathSensitivity.NAME_ONLY) //это для кэша
    @get: InputDirectory
    abstract val inputDir: DirectoryProperty

    @get: OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    open fun prefixFileLines(inputChanges: InputChanges) {
        //
        println("is incremental: ${inputChanges.isIncremental}")
        println(
            if (inputChanges.isIncremental) "Executing incrementally"
            else "Executing non-incrementally"
        )

        //Встретились с такой ошибкой:
        //connot query incremental changes: No property found for value file collection
        //не удается запросить инкрементные изменения: не найдено свойство для коллекции файлов значений

        //getFileChanges - можно получить только для проперти помеченного как Incremental
        inputChanges.getFileChanges(inputDir).forEach {
            //если мы в диретории а не в файле то выйдем из неё, т.е. скипнем,
            //а если у нас файл, то изменим его далее.
            if (it.fileType == FileType.DIRECTORY) return@forEach
            println("${it.changeType} --- ${it.normalizedPath}")

            val targetFile = outputDir.file(it.normalizedPath).get().asFile
            if (it.changeType == ChangeType.REMOVED) {
                targetFile.delete()
            } else {
                val outputFileContent = it.file.readLines() //List<String> - читаем строки из файла
                    .joinToString("\n") { "prefix: $it" }
                targetFile.writeText(outputFileContent)
            }
        }
    }
}