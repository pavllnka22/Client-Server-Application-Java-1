package package1;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

class MultiThreadTest {

    @Test
    void shouldProcessMultiThreadedPacketsWithoutDeadlocks() throws Exception {

        BlockingQueue<byte[]> rawQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Message> decryptedQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> encryptedQueue = new LinkedBlockingQueue<>();

        ExecutorService executor = Executors.newFixedThreadPool(10);

        executor.submit(new Decriptor(rawQueue, decryptedQueue));
        executor.submit(new Decriptor(rawQueue, decryptedQueue));

        executor.submit(new Processor(decryptedQueue, responseQueue));
        executor.submit(new Processor(decryptedQueue, responseQueue));

        executor.submit(new Encriptor(responseQueue, encryptedQueue));
        executor.submit(new Encriptor(responseQueue, encryptedQueue));

        executor.submit(new Sender(encryptedQueue));

        Encrypter testEncrypter = new Encrypter();
        int packetsCount = 55;
        CountDownLatch latch = new CountDownLatch(packetsCount);

        for (int i = 0; i < packetsCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Message msg = new Message((byte) 0x12, index, 2, 67, "Test Message");
                    byte[] packet = testEncrypter.encrypt(msg);
                    rawQueue.put(packet);
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        executor.shutdownNow();
    }
}
