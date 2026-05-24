package package1;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SafeIncrementTest {
    @Test
    void shouldIncrementStockCountSafely() throws Exception {

        java.util.concurrent.atomic.AtomicInteger itemsInStock = new java.util.concurrent.atomic.AtomicInteger(67);

        int numberOfParallelRequests = 700;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(numberOfParallelRequests);

        for (int i = 0; i < numberOfParallelRequests; i++) {
            executor.submit(() -> {
                try {
                    itemsInStock.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(3, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(itemsInStock.get()).isEqualTo(767);
    }
}
