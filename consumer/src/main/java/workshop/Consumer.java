package workshop;

import com.google.common.base.Strings;
public class Consumer {
    public static void main(String[] args) {
        System.out.println("Hello from Consumer");

        //здесь просто проверим, что библиотека guava нормально зашла
        System.out.println(Strings.repeat("a", 50));

        Producer.main(args);
    }
}
