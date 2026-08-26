# PSP · Concurrencia e hilos

Colección reimplementada de los ejemplos de PSP sobre creación de hilos, cajeras y productor/consumidor. Se sustituyen bucles de espera y coordinación manual innecesaria por las primitivas de `java.util.concurrent` apropiadas.

Incluye:
- ejecución con `ExecutorService`;
- productor/consumidor con `BlockingQueue`;
- procesamiento concurrente de una cola por varias cajeras;
- contador seguro con `AtomicInteger`.

```bash
mvn verify
java -cp target/classes dev.rodrigosambade.concurrent.ConcurrencyDemo
```
