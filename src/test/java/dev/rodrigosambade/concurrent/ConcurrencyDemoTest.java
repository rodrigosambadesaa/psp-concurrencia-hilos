package dev.rodrigosambade.concurrent;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrencyDemoTest {

    @Test
    void producerConsumerPreservesItems() throws InterruptedException {
        List<Integer> consumed = ConcurrencyDemo.producerConsumer(5);

        assertEquals(List.of(0, 1, 2, 3, 4), consumed);
    }

    @Test
    void checkoutProcessesEveryItemExactlyOnce() throws InterruptedException {
        Map<String, Integer> processedByCashier = ConcurrencyDemo.processCheckout(
                List.of(0, 0, 0, 0, 0, 0),
                3);

        int processedItems = processedByCashier.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(6, processedItems);
    }
}
