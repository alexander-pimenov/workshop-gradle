plugins {
    base

    //т.к. мы описали публикацию плагина в gradlePlugin
    // в buildSrc/build.gradle.kts, то здесь можно подключить плагин
    //через его id = "workshop.java-plugin"
    //и этот плагин будет участвовать в поиске на генерацию dsl.
    id("workshop.java-plugin")
    id("workshop.java-library-plugin")

    //подключим стандартный мавен плагин для публикации
    // (если мы хотим поделиться со всем миром своим плагином)
    //У Gradle есть возможность публиковать конкретные артефакты.
    //Т.е. можно сказать, что "я хочу опубликовать конкретно этот jar".
    `maven-publish`
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
    //add("implementation", JavaPlugin::class.java) - но это хардкод, так уже не делают.

    //используем dsl - implementation
    //implementation("com.google.guava:guava:31.0-jre")

    //после добавления плагина "workshop.java-library-plugin" можно использовать dsl - api
    //и api конфигурация притаскивает с собой guava, как api, транзитивно.
    //А вот с implementation перестанет компилироваться.
    //api - это очень требовательная зависимость
    //и если мы сделали guava, как api, то мы будем всю жизнь жить с guava и не сможем от неё отказаться.
    //api вместе с нашей либой транзитивно приносит и другие библиотеки.
    //лучше всегда явно использовать implementation если нужны какие-то зависимости.
    //Например, если для Consumer нужна guava, то нужно её подключить через implementation
    //у Consumer в build.gradle.kts и не зависеть от транзитивных зависимостей.
    api("com.google.guava:guava:31.0-jre")
}


repositories {
    mavenCentral()
}

//Про publishing можно почитать тут:
//https://docs.gradle.org/current/userguide/publishing_maven.html
//Что бы что-то опубликовалось с метаинформацией, нам нужно сделать компонент.

publishing{
    publications{
        //передаем имя публикации, например, java
        create<MavenPublication>("java"){
            //тут говорим, что мы хоти опубликовать
            //1-й вариант:
            // например, чисто теоретически можно указать артефакт, который хотим опубликовать
            // указывая откуда хотим брать артефакт
            //Например, таску jar (заккоментировал ,т.к. это не используется)
//            artifact(tasks.jar){
                ////указываем версию и т.д.
                ////version = ""
//            }

            //2-й вариант (так и делают):
            //хотим опубликовать компонент, в котором есть всё что нам нужно
            from(components["java"])

            //тут указываем то какие наши атрибуты оутгониг вариантов на какие будут мапится
//            versionMapping {
//                usage("java-api") {
//                    //fromResolutionOf("classpath") - можно указать имя
//                    fromResolutionOf(configurations.classpath.name) //можно написать и так
//                }
//                usage("java-runtime") {
//                    fromResolutionResult()
//                }
//            }
        }

    }
}