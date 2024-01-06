plugins {
    base

    //т.к. мы описали публикацию плагина в gradlePlugin
    // в buildSrc/build.gradle.kts, то здесь можно подключить плагин
    //через его id = "workshop.java-plugin"
    //и этот плагин будет участвовать в поиске на генерацию dsl.
    id("workshop.java-plugin")
}

//тут применим наш самописный плагин для компиляции классов
//для вызова таски через плагин для Producer вызываем команду:
// ".\gradlew clean producer:compile"
//compile - с этим именем мы зарегистрировали таску в плагине.
// Но его закомментировали, т.к. мы описали публикацию этого плагина в
// gradlePlugin в buildSrc/build.gradle.kts и объявили его подключение
// в блоке plugins {} , который выше, через его id, описанный в gradlePlugin
// в buildSrc/build.gradle.kts
//apply<workshop.JavaPlugin>()

//объявим блок dependencies
dependencies {
    //можно указать добавление зависимостей так:
    //add("implementation", JavaPlugin::class.java) - но это хардкод

    //используем dsl
    implementation("com.google.guava:guava:31.0-jre")
}


repositories {
    mavenCentral()
}