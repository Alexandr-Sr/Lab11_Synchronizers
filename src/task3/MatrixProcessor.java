package task3;

import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ЛР11 — Задание 3 (CyclicBarrier)
 * Параллельная обработка матрицы 9x3 тремя потоками в 3 этапа.
 * Этапы:
 *  1) умножение на 2
 *  2) прибавление 10
 *  3) деление на 3
 * После каждого этапа потоки синхронизируются на барьере.
 * JDK 21+, UTF-8.
 */
public class MatrixProcessor {

    // Матрица 9x3 со значениями 1..27
    private static final int ROWS = 9;
    private static final int COLS = 3;
    private static final int THREADS = 3;
    private static final int[][] matrix = new int[ROWS][COLS];

    public static void main(String[] args) {
        init();
        System.out.println("Начальная матрица:");
        printMatrix();

        AtomicInteger stage = new AtomicInteger(0);
        // Действие барьера: печать завершения этапа + матрица
        CyclicBarrier barrier = new CyclicBarrier(THREADS, () -> {
            int s = stage.incrementAndGet();
            System.out.printf("=== Этап %d завершён ===%n", s);
            printMatrix();
        });

        Thread[] workers = new Thread[THREADS];
        int rowsPerThread = ROWS / THREADS; // 3 строки на поток

        for (int i = 0; i < THREADS; i++) {
            final int startRow = i * rowsPerThread;
            final int endRow = startRow + rowsPerThread; // не включая
            final int tid = i + 1;
            workers[i] = new Thread(() -> processChunk(tid, startRow, endRow, barrier), "matrix-worker-" + tid);
            workers[i].start();
        }

        // Ждём завершения всех потоков
        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Главный поток: прерван во время ожидания.");
            }
        }

        System.out.println("Обработка матрицы завершена.");
    }

    private static void processChunk(int id, int startRow, int endRow, CyclicBarrier barrier) {
        try {
            // Этап 1 — умножение на 2
            log("Поток-%d: Выполняет этап 1 (строки %d-%d)", id, startRow, endRow - 1);
            for (int r = startRow; r < endRow; r++) {
                for (int c = 0; c < COLS; c++) {
                    matrix[r][c] *= 2;
                }
            }
            barrierAwait(barrier);

            // Этап 2 — прибавление 10
            log("Поток-%d: Выполняет этап 2 (строки %d-%d)", id, startRow, endRow - 1);
            for (int r = startRow; r < endRow; r++) {
                for (int c = 0; c < COLS; c++) {
                    matrix[r][c] += 10;
                }
            }
            barrierAwait(barrier);

            // Этап 3 — деление на 3
            log("Поток-%d: Выполняет этап 3 (строки %d-%d)", id, startRow, endRow - 1);
            for (int r = startRow; r < endRow; r++) {
                for (int c = 0; c < COLS; c++) {
                    matrix[r][c] /= 3;
                }
            }
            barrierAwait(barrier);

        } catch (Exception e) {
            log("Поток-%d: ошибка: %s", id, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static void barrierAwait(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void init() {
        int val = 1;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                matrix[r][c] = val++;
            }
        }
    }

    private static void printMatrix() {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static void log(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), msg);
    }
}
