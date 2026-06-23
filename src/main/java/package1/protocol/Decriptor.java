package package1.protocol;

import java.util.concurrent.BlockingQueue;

public class Decriptor implements Runnable {
    private final BlockingQueue<byte[]> inputQueue;
    private final BlockingQueue<Message> outputQueue;
    private final Decrypter decrypter = new Decrypter();

    public Decriptor(BlockingQueue<byte[]> inputQueue, BlockingQueue<Message> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] rawBytes = inputQueue.take();
                Message decryptedMessage = decrypter.decrypt(rawBytes);
                outputQueue.put(decryptedMessage);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}