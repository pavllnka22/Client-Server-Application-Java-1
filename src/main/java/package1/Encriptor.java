package package1;

import java.util.concurrent.BlockingQueue;

public class Encriptor implements Runnable {

    private final BlockingQueue<Message> inputQueue;
    private final BlockingQueue<byte[]> outputQueue;
    private final Encrypter encrypter = new Encrypter();

    public Encriptor(BlockingQueue<Message> inputQueue, BlockingQueue<byte[]> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Message response = inputQueue.take();
                byte[] encryptedResponse = encrypter.encrypt(response);
                outputQueue.put(encryptedResponse);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
