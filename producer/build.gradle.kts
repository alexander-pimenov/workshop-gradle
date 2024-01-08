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

//Т.к. мы добавили extension, с именем java для задания версии,
// то можем это использовать и проверить, что оно работает:
java {
    javaVersion.set(8)
}

repositories {
    mavenCentral()
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
    //Посмотреть документацию: https://docs.gradle.org/current/userguide/rich_versions.html
//    implementation("com.google.guava:guava") {
//        version {
//            strictly("31.0-jre, 32.1.3-jre[")
//            prefer("31.1-jre")
//        }
//    } //

    //api("com.google.guava:guava:31.0-jre")

    //После добавления platform мы теперь можем не указывать версию, т.к. она возьмется из платформы
    api("com.google.guava:guava")

    //После добавления platform мы теперь можем добавить здесь в producer платформу из проекта platform:
    api(platform(project(":platform")))

    //Можем получить рекомендуемые версии из проекта платформы:
    //api(platform(project(":platform")))

    //Пример import a BOM
    //implementation(platform("org.springframework.boot:spring-boot-dependencies:1.5.8.RELEASE"))

    //Пример import a BOM. Версии, используемые в этом файле, будут переопределять любую другую версию, найденную в графе зависимостей.
    //но это может что-то сломать, т.к. например, библиотека просит одну версию, а вы жестко ставите ей другую.
    //implementation(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:1.5.8.RELEASE"))

    /*
    * В Gradle существует аналог Maven Bom, и это Platform.
    * Это отдельный компонент со своими вариантами, который публикует что-то типа BOM файла.
    * Т.е. он описывает какие версии компонентов чему должны быть равны.
    * platform - добавляет атрибут к нашей зависимости и достает вариант с публикацией platform.
    * platform - позволяет нам описать, какие версии мы рекомендуем для использования.
    * Версию зависимости можно не указывать, потому что версия будет взята из platform.
    * api("com.google.guava:guava")
    * А если мы укажем версию api("com.google.guava:guava:31.0-jre") то тогда возможен конфликт
    * версий, если они не совпадут.
    * В Gradle сделано так, что наиболее близкая по транзитивности к корню зависимость выигрывает.
    * //Можно почитать в документации: https://docs.gradle.org/current/userguide/rich_versions.html
    * И еще в Gradle можно указать не только определенную зависимость, а указать диапазон (range) версий:
    *  api("com.google.guava:guava") {
    *     version {
    *        strictly("31.0-jre, 32.1.3-jre[")
    *      prefer("31.1-jre")
    *    }
    *  } //
    *
    * и Gradle выберет такую, которая будет удовлетворять всех наших транзитивных друзей.
    *
    * Это сделано для того, чтобы можно было резолвить (разрешивать) конфликты версий.
    */
}

//когда мы резолвим какую-нибудь конфигурацию и хотим, например, применить версию для всех,
// то можно записать так:
//"Все конфигурации, которые у меня есть:"
configurations.all {
    //"напишу для них свою резолюшен стратегию".
    //Резолюшен Стратегия - это программный способ управлением зависимостями.
    //Через resolutionStrategy можно одну версию приходящей зависимости заменить на другую,
    //т.е. подлезть в самое ядро и сделать замену.
    //А если эту настройку resolutionStrategy добавить в плагин, то это будет
    //делаться автоматически во время использования плагина.
    /*
    ***Например, взято из документации:***
    В случае конфликта Gradle по умолчанию использует самую новую из конфликтующих версий.
    Однако вы можете изменить это поведение. Используйте этот метод, чтобы настроить
    разрешение на быстрый сбой при любом конфликте версий, например, при наличии нескольких
    разных версий одной и той же зависимости (группа и имя равны) в одной и той же конфигурации.
    Проверка включает как зависимости первого уровня, так и переходные зависимости.
    Например:
      plugins {
        id 'java' // so that there are some configurations
      }
      configurations.all {
          resolutionStrategy.failOnVersionConflict()
      }
    Или можно добавить правило замены зависимостей, которое запускается для каждой зависимостей
    (включая транзитивные) при разрешении конфигурации.
    Example:
    configurations {
      compileClasspath.resolutionStrategy {
          eachDependency { DependencyResolveDetails details ->
            //specifying a fixed version for all libraries with 'org.gradle' group
            if (details.requested.group == 'org.gradle') {
                details.useVersion '1.4'  // прибиваем версию 1.4
          }
      }
      eachDependency { details ->
          //multiple actions can be specified
          if (details.requested.name == 'groovy-all') {
              //changing the name:
              details.useTarget group: details.requested.group, name: 'groovy', version: details.requested.version
          }
        }
      }
    }
    Правила вычисляются в порядке их объявления. Правила вычисляются после применения принудительных модулей.
    */
    resolutionStrategy {
        //указываю зависимость
        force("com.google.guava:guava:31.0-jre")
    }

}


//Про publishing можно почитать тут:
//https://docs.gradle.org/current/userguide/publishing_maven.html
//Что бы что-то опубликовалось с метаинформацией, нам нужно сделать компонент.

publishing {
    publications {
        //передаем имя публикации, например, java
        create<MavenPublication>("java") {
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


