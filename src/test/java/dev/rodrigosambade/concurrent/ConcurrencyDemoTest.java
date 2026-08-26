package dev.rodrigosambade.concurrent;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ConcurrencyDemoTest {@Test void producerConsumerPreservesItems()throws Exception{assertEquals(List.of(0,1,2,3,4),ConcurrencyDemo.producerConsumer(5));}@Test void checkoutProcessesEverythingOnce()throws Exception{assertEquals(6,ConcurrencyDemo.processCheckout(List.of(0,0,0,0,0,0),3).values().stream().mapToInt(Integer::intValue).sum());}}
