package workshop

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

/**
 * Пишем таску, которая это будет компилировать классы.
 * Т.е. выполнять то, как мы компилируем классы из командной строки
 * с помощью команды:<br>
 * <code>javac -sourcepath producer/src/main/java -d producer/build/classes producer/src/main/java/workshop/Producer.java</code><br>
 * Т.к. нам понадобится писать execute процесса, то лучше отнаследоваться от AbstractExecTask,
 * а не от DefaultTask(), т.к. она позволяет, что-то запускать. Т.е. набить команды,
 * которые будем запускать.
 */
@CacheableTask
abstract class CompileJavaTask : AbstractExecTask<CompileJavaTask>(CompileJavaTask::class.java) {


    // * producer/src/main/java/workshop/*.java --- список всех source файлов (у нас есть один)

    //задаем sourcepath, т.е. источник классов, это директория откуда считываем классы
    // -sourcepath producer/src/main/java --- место источник классов для компиляции
    @get: InputDirectory
    @get: PathSensitive(PathSensitivity.RELATIVE) //это для кэша, RELATIVE - относительно нашего билда
    abstract val sourceDir: DirectoryProperty

    //задаем директорию куда сложим скомпилированные классы
    // -d producer/build/classes --- output директория, т.е. куда сложить скомпилированные файлы, сделали по аналогии с Gradle
    @get: OutputDirectory
    abstract val outputDir: DirectoryProperty

    //задаем classpath, и это у нас есть список артефактов,
    //поэтому для него используем конфигурированную коллекцию - ConfigurableFileCollection
    @get: InputFiles
    @get: PathSensitive(PathSensitivity.RELATIVE) //это для кэша, RELATIVE - относительно нашего билда
    abstract val classPath: ConfigurableFileCollection

    //Для того чтобы получить доступ к директории нужно использовать
    //возможности Project, но это тоже не совсем правильно, т.к. конфигурация кэшируется
    //и поэтому нужен ProjectLayout
    @get: Inject
    abstract val layout: ProjectLayout

    //воспользуемся блоком init{} и сконфигурируем таску, укажем sourceDirectory и outputDirectory
    init {
        //перенесем таску в группу build (это в меню Gradle)
        group = "build"
        sourceDir.set(layout.projectDirectory.dir("src/main/java").asFile)
        outputDir.set(layout.projectDirectory.dir("build/classes").asFile)
    }

    //переопределим таску из AbstractExecTask, т.к. она там protected , а нам нужно её
    //немного кастомизировать.
    //Напомню, что иметь несколько функций с @TaskAction в одном классе это не хорошо из-за
    //сложного контроля над их выполнением.
    //@TaskAction
    override fun exec() {
        //Настраиваем нашу команду.
        //устанавливаем параметры команды
        executable = "javac"
        val outDir = outputDir.get().asFile
        Files.createDirectories(outDir.toPath())
        //дальше аргументы нужно передать списком как в командной строке
        val args = mutableListOf<String>()
        //добавим sourcepath
        args.add("-sourcepath")
        //укажем директорию для source через layout
        args.add(sourceDir.get().asFile.toProjectRelativeString())
        //добавим output
        args.add("-d")

        //укажем директорию для out
        args.add(outDir.toProjectRelativeString())
        //добавим classpath если он не пустой
        if (!classPath.isEmpty) {
            args.add("-cp")
            //список файлов, возьмем их paths и сджойним через разделитель
            args.add(classPath.files.joinToString(File.pathSeparator) { it.path })
        }
        //добавляем последний аргумент команды - сами классы, которые нужно компилировать
        args.add(sourceDir.asFileTree.joinToString(" ") { it.toProjectRelativeString() })


        //теперь можно засетить аргументы
        setArgs(args)

        //и теперь вызываем super-функцию
        super.exec()
    }

    private fun File.toProjectRelativeString(): String = toRelativeString(layout.projectDirectory.asFile)
}