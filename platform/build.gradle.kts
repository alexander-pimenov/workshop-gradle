plugins {
    `java-platform`
    //скажем, что она публикуется:
    `maven-publish`
}

//описываем какие зависимости она с собой будет приносить
dependencies {
    constraints {
        //если кто-то использует конфигурацию api, то
        //пожалуйста применяй версию guava вот такой:
        api("com.google.guava:guava:31.0-jre")
    }
}

//Можно таким образом описать для автоматической постройки платформы для всех модулей проекта:
//взять рутовый проект, и для его подпроектов, кроме платформы, добавить все зависимости
//в api
//dependencies {
//    constraints {
//        rootProject.subprojects.filter { it != project }.forEach {
//            api(project(":${it.name}"))
//        }
//    }
//}

//После публикации, например, после запуска таски publishToMavenLocal
//мы сможем увидеть в репозитории platform компонент с файлами:
//"...\platform\1.0.0-SNAPSHOT\maven-metadata-local.xml"
//"...\platform\1.0.0-SNAPSHOT\platform-1.0.0-SNAPSHOT.module"
//"...\platform\1.0.0-SNAPSHOT\platform-1.0.0-SNAPSHOT.pom"
publishing {
    publications {
        create<MavenPublication>("javaPlatform") {
            from(components["javaPlatform"])
        }
    }
}