plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

//опишем какой плагин даем наружу, т.е. публикация плагина
gradlePlugin {
    plugins {
        //дадим имя плагину
        create("java-plugin") {
            //дадим id плагину, и он важнее чем имя
            id = "workshop.java-plugin"
            implementationClass = "workshop.JavaPlugin"
        }

        //дадим имя плагину
        create("java-library-plugin") {
            //дадим id плагину, и он важнее чем имя
            id = "workshop.java-library-plugin"
            implementationClass = "workshop.JavaLibraryPlugin"
        }
    }
}