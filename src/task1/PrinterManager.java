package task1;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * ЛР11 — Задание 1 (Semaphore)
 * Управление доступом к 3 принтерам для 7 сотрудников.
 */
public class PrinterManager {

    private static final int PRINTERS = 3;
    private static final int EMPLOYEES = 7;

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(PRINTERS, true); // честная политика (FIFO)
        Random rnd = new Random();

        Thread[] workers = new Thread[EMPLOYEES];
        for (int i = 0; i < EMPLOYEES; i++) {
            final int id = i + 1;
            workers[i] = new Thread(() -> {
                log("Сотрудник-%d: Ожидает доступа к принтеру...", id);
                try {
                    semaphore.acquire();
                    log("Сотрудник-%d: Начинает печать", id);
                    // "Печать" занимает 1-3 сек
                    TimeUnit.SECONDS.sleep(1 + rnd.nextInt(3));
                    log("Сотрудник-%d: Завершил печать", id);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log("Сотрудник-%d: Прерван во время ожидания/печати", id);
                } finally {
                    // Гарантированно освобождаем "принтер", если он захвачен
                    if (semaphore.availablePermits() < PRINTERS) {
                        semaphore.release();
                    }
                }
            }, "employee-" + id);
            workers[i].start();
        }

        // Ждём завершения потоков
        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log("Главный поток: прерван во время ожидания сотрудников");
            }
        }

        log("Главный поток: Все задания на печать завершены.");
    }

    private static void log(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), msg);
    }
}
