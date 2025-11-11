package task2;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ЛР11 — Задание 2 (CountDownLatch)
 * Загрузка данных из 4 источников перед стартом приложения.
 * JDK 21+, UTF-8.
 */
public class DataLoader {

    public static void main(String[] args) {
        String[] sources = {"База данных", "Конфигурация", "Кэш", "API"};
        CountDownLatch latch = new CountDownLatch(sources.length);
        Random rnd = new Random();

        // Запускаем загрузчики
        for (String src : sources) {
            new Thread(() -> {
                log("%s: Начинается загрузка...", src);
                try {
                    // Эмулируем время загрузки 1–4 сек
                    TimeUnit.SECONDS.sleep(1 + rnd.nextInt(4));
                    log("%s: Загрузка завершена", src);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log("%s: Прервано во время загрузки", src);
                } finally {
                    latch.countDown();
                }
            }, "loader-" + src).start();
        }

        log("Главный поток: Ожидание завершения всех загрузок...");
        try {
            latch.await(); // ждём, пока счётчик не станет 0
            log("Главный поток: Все данные загружены! Приложение запущено.");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log("Главный поток: Прерван во время ожидания загрузок");
        }
    }

    private static void log(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), msg);
    }
}
