package package1;

import org.apache.commons.codec.digest.DigestUtils;
import package1.protocol.Decriptor;
import package1.protocol.Encriptor;
import package1.protocol.Message;
import package1.server.Receiver;
import package1.server.Sender;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class App {

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<byte[]> rawQueue = new LinkedBlockingQueue<>(100);
        BlockingQueue<Message> decryptedQueue = new LinkedBlockingQueue<>(100);
        BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<>(100);
        BlockingQueue<byte[]> encryptedQueue = new LinkedBlockingQueue<>(100);

        int receiversCount = 2;
        int decriptorsCount = 2;
        int processorsCount = 4;
        int encriptorsCount = 3;
        int sendersCount = 5;

        int totalThreads = receiversCount + decriptorsCount + processorsCount + encriptorsCount + sendersCount;
        ExecutorService threadPool = Executors.newFixedThreadPool(totalThreads);

        for (int i = 0; i < receiversCount; i++) threadPool.submit(new Receiver(rawQueue));
        for (int i = 0; i < decriptorsCount; i++) threadPool.submit(new Decriptor(rawQueue, decryptedQueue));
        //for (int i = 0; i < processorsCount; i++) threadPool.submit(new Processor(decryptedQueue, responseQueue));
        for (int i = 0; i < encriptorsCount; i++) threadPool.submit(new Encriptor(responseQueue, encryptedQueue));
        for (int i = 0; i < sendersCount; i++) threadPool.submit(new Sender(encryptedQueue));


        Thread.sleep(4000);

        threadPool.shutdownNow();

    }

    public static String sha256hex(String input) {
        return DigestUtils.sha256Hex(input);
    }

}

