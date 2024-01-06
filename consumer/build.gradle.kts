plugins {
    java
}

//РЕКОМЕНДУЕТСЯ все зависимости, которые нужно использовать заимпортить явно,
//а не надеяться на транзитивно подтягиваемые зависимости.
//Об этом говорится тут:
//https://youtu.be/yDj0n0g5dXY?t=11262
dependencies {
    //подключим в Consumer проект Producer, т.е. создаем зависимость на наш подмодуль
    //и хотим использовать его артефакт, т.е. результат его работы.
    //Вариант с указанием конкретной конфигурацией "runtimeElements" не стоит использовать,
    // т.к. очень легко всё сломать, но оставлю для примера:
    //implementation(project(":producer", "runtimeElements"))

    implementation(project(":producer")) {
        //тут можно указать те атрибуты, которые нам нужно явно употребить
        // producer:outgoingVariants производит аттрибуты в нашем JavaPlugin
        attributes {
            //оставим пустым для примера
        }
    }

    //implementation("com.google.guava:guava:33.0.0-jre")
}

repositories {
    mavenCentral()
}

