package workshop;

import com.google.common.base.Strings;

/**
 * Сначало этот класс пишем, как текстовый файл с расширением java.
 * Затем в командной строке компилируем с спомощью команды:
 * {@code javac -sourcepath producer/src/main/java -d producer/build/classes producer/src/main/java/workshop/*.java}
 * поясняем:
 * javac --- утилита java для компиляции
 * -sourcepath producer/src/main/java --- место источник классов для компиляции
 * -d producer/build/classes --- output директория, т.е. куда сложить скомпилированные файлы, сделали по аналогии с Gradle
 * producer/src/main/java/workshop/*.java --- список всех source файлов (у нас есть один)
 *
 * Наша задача написать плагин, который это будет делать.
 * Переходим в buildSrc и создаем новую таску.
 *
 *
 */
public class Producer {
    public static void main(String[] args) {
        System.out.println("Hello from Producer");

        System.out.println(Strings.repeat("B", 20));
    }
}