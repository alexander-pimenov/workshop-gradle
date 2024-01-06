import workshop.FileLinePrefixing2Task
import workshop.FileLinePrefixingTask
import workshop.IncrementalReverseTask
import workshop.FileLinePrefixingPlugin

plugins {
    //добавляет таску build с assemble, build, clean в блоке Gradle
    base
    //если он есть в settings.gradle, то версию можно не ставить
    //id("ru.sberbank.uvz3.gradle.jvm")
}

apply<FileLinePrefixingPlugin>()

//зарегистрируем таску: несколько способов сделать это
//val myTask by tasks.creating {}
tasks.create("myTask") {

    //Здесь можно еще настроить таску
    println("Configuration time $this")

    //добавить акшен в конец
    doLast {
        //так же посмотрим какие проперти есть у проекта по дефолту project.properties
        //их много поэтому закомментируем их:
//        println("Hello I am here! ${project.properties}")
        //задать её можно с помощью -P так: .\gradlew myTask -PmyProp=123
        println("I'm waiting here for the property:  ${project.property("myProp")}")
    }
}


//это типичный для билдскриптов колбэк, что бы посмотреть, когда
//проект будет сэвалюирован
afterEvaluate {
    println("Project evaluated") //Оценка проекта
}

//зарегистрируем еще таску
tasks.register("myTask2") {
    println("Configuration time Task2")
    doLast {
        println("Task2")
    }
}

//зарегистрируем еще таску, но еще другим методом
//val myTask3: TaskProvider<Task> by tasks.registering { - ниже укороченная запись:
val myTask3 by tasks.registering {
    println("Configuration time $this")
    dependsOn("myTask2")
    doLast {
        println("$this")
    }
}

//зарегистрируем еще таску
//val myTask4: TaskProvider<Task> by tasks.registering { - ниже укороченная запись
//Здесь указан способ очередности тасок через mustRunAfter
//но при запуске .\gradlew myTask4 выполнится только myTask4,
// а при запуске .\gradlew myTask4 myTask2
//будут выполнены обе таски
val myTask4 by tasks.registering {
    println("Configuration time $this")
    mustRunAfter("myTask2")
    doLast {
        println("$this")
    }
}

// Нужно отметить что лучше использовать кастомные классы тасок, т.е. для таски выделяем класс,
// они более эффективны, нежели dsl-таски.
// Потому что у обычной dsl-таски есть массив inputs и мы не можем сказать какой именно из
// файлов что значит - т.е. работа постоена примерно так, мы говорим:
// 1-й индекс outcome определенной таски я передаю в 3-й индекс input другой таски.
// Это очень сложно поддерживать, т.к. даже просто поменяем набор параметров и весь билд развалится.
//Отдельные кастомные классы лучше помещать в папку 'buildSrc' и это gradle воспринимает, как
// includeBuild в файле settings.gradle.kts

//если мы хотим использовать свои конкретные таски, то использовать `tasks.registering`
//уже нельзя, т.к. нужно указывать тип нашей таски, а нужно уже с передаваемым типом:
//tasks.registering(type: KClass<U>)
//
val fileLinePrefixingTask by tasks.registering(FileLinePrefixingTask::class) {
    println("Configuration time $this")
    //через метод set говорим откуда взять сам файл для вычитки
    //inputFile.set(file("files/file1.txt")) //это не очень хороший способ
    inputFile.set(layout.projectDirectory.file("filesss/file1.txt")) //этот способ более верный
    //через метод set говорим куда положить файл после преобразования
    outputFile.set(layout.buildDirectory.file("filesss/file1.txt")) //этот способ более верный
}

val fileLinePrefixing2Task by tasks.registering(FileLinePrefixing2Task::class) {
    println("Configuration time $this")
    //через метод set говорим откуда взять файл/файлы для вычитки, т.е. указываем директорию
    //1-й способ:
//    inputDir.set(layout.projectDirectory.dir("files"))
    //2-й способ:
    inputDir.set(file("files"))
    //через метод set говорим куда положить файл/файлы после преобразования, в какую директорию
    //1-й способ:
//    outputDir.set(layout.buildDirectory.dir("files"))
    //2-й способ:
    outputDir.set(file("$buildDir/files"))
}

val incrementalReverseTask by tasks.registering(IncrementalReverseTask::class) {
    //!!! взятая из туториала таска не дала скомпилить закомментированную ниже
    //запись. Переделал на ту что ниже закомментированого кода
    //inputDir = file("inputs")
    //outputDir = layout.buildDirectory.dir("outputs")
    //inputProperty = project.findProperty("taskInputProperty") as String? ?: "original"
    inputDir.set(layout.projectDirectory.dir("inputs"))
    outputDir.set(layout.buildDirectory.dir("outputs"))
    inputProperty.set(project.findProperty("taskInputProperty") as String? ?: "original")
}



repositories {
    mavenCentral()
}

