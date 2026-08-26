package dev.rodrigosambade.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConcurrencyDemo {

    private static final int END_OF_STREAM = -1;
    private static final int MAX_QUEUE_CAPACITY = 32;

    private ConcurrencyDemo() {
    }

    public static List<Integer> producerConsumer(int count) throws InterruptedException {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }

        int capacity = Math.max(1, Math.min(MAX_QUEUE_CAPACITY, count + 1));
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(capacity);
        List<Integer> consumed = Collections.synchronizedList(new ArrayList<>());

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> producer = executor.submit(() -> produce(queue, count));
            Future<?> consumer = executor.submit(() -> consume(queue, consumed));

            await(producer);
            await(consumer);
        }

        return List.copyOf(consumed);
    }

    public static Map<String, Integer> processCheckout(
            List<Integer> itemTimesMs,
            int cashiers) throws InterruptedException {
        Objects.requireNonNull(itemTimesMs, "itemTimesMs");
        if (cashiers <= 0) {
            throw new IllegalArgumentException("cashiers must be greater than zero");
        }

        Queue<Integer> pendingItems = new ConcurrentLinkedQueue<>(itemTimesMs);
        Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(cashiers)) {
            List<Future<?>> tasks = new ArrayList<>(cashiers);

            for (int index = 1; index <= cashiers; index++) {
                String cashierName = "cashier-" + index;
                AtomicInteger processedItems = new AtomicInteger();
                counters.put(cashierName, processedItems);

                tasks.add(executor.submit(
                        () -> processItems(pendingItems, processedItems)));
            }

            for (Future<?> task : tasks) {
                await(task);
            }
        }

        return immutableCounts(counters);
    }

    private static void produce(BlockingQueue<Integer> queue, int count) {
        try {
            for (int value = 0; value < count; value++) {
                queue.put(value);
            }
            queue.put(END_OF_STREAM);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Producer interrupted", exception);
        }
    }

    private static void consume(
            BlockingQueue<Integer> queue,
            List<Integer> consumed) {
        try {
            while (true) {
                int value = queue.take();
                if (value == END_OF_STREAM) {
                    return;
                }
                consumed.add(value);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consumer interrupted", exception);
        }
    }

    private static void processItems(
            Queue<Integer> pendingItems,
            AtomicInteger processedItems) {
        Integer delayMs;
        while ((delayMs = pendingItems.poll()) != null) {
            try {
                Thread.sleep(Math.max(0, delayMs));
                processedItems.incrementAndGet();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void await(Future<?> task) throws InterruptedException {
        try {
            task.get();
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Concurrent task failed", exception.getCause());
        }
    }

    private static Map<String, Integer> immutableCounts(
            Map<String, AtomicInteger> counters) {
        Map<String, Integer> result = new TreeMap<>();
        counters.forEach((name, count) -> result.put(name, count.get()));
        return Map.copyOf(result);
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> produced = producerConsumer(10);
        Map<String, Integer> checkout = processCheckout(
                List.of(10, 20, 5, 15, 10, 5),
                2);

        System.out.println("Productor/consumidor: " + produced);
        System.out.println("Cajeras: " + checkout);
    }
}
