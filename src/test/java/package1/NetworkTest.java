package package1;


import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

class NetworkTest {

    @Test
    void testMultipleTCPClientsWithReconnection() throws InterruptedException {
        int port = 8888;

        Thread serverThread = new Thread(() -> new StoreServerTCP(port).start());
        serverThread.start();
        Thread.sleep(700);

        int clientsCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(clientsCount);

        for (int i = 0; i < clientsCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                StoreClientTCP client = new StoreClientTCP("localhost", port);
                Message request = new Message((byte) 0x12, clientId, 1, clientId, "Grechka Request");

                Message response = client.sendRequest(request);
                assertThat(response).isNotNull();
                assertThat(response.getEncryptedMessage()).isEqualTo("OK");
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        serverThread.interrupt();
    }

    @Test
    void testUDPRetryMechanismOnPacketLoss() {
           StoreClientUDP client = new StoreClientUDP("localhost", 6969);
        Message request = new Message((byte) 0x12, 1L, 22, 57, "TESTT");

        long startTime = System.currentTimeMillis();
        Message response = client.sendRequestWithRetry(request);
        long duration = System.currentTimeMillis() - startTime;

        assertThat(response).isNull();
        assertThat(duration).isGreaterThanOrEqualTo(6000);
    }
}
