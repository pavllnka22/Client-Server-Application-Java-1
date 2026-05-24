package package1;

import org.apache.commons.codec.binary.Hex;
import java.util.concurrent.BlockingQueue;

public class Sender implements Runnable {
    private final BlockingQueue<byte[]> inputQueue;

    public Sender(BlockingQueue<byte[]> inputQueue) {
        this.inputQueue = inputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] responsePacket = inputQueue.take();
                System.out.println("Encrypted message: " + Hex.encodeHexString(responsePacket));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
