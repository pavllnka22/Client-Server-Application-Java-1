package package1.server;

import package1.protocol.Message;
import package1.protocol.Encrypter;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Receiver implements Runnable {
    private final BlockingQueue<byte[]> outputQueue;
    private final Random random = new Random();
    private final Encrypter encrypter = new Encrypter();

    public Receiver(BlockingQueue<byte[]> outputQueue) {
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int commandId = random.nextInt(1,6) ;
                Message networkMessage = new Message(
                        (byte) 0x12,
                        random.nextLong(777),
                        commandId,
                        random.nextInt(222),
                        "Payload data for command " + commandId
                );

                byte[] rawPacket = encrypter.encrypt(networkMessage);
                outputQueue.put(rawPacket);

                Thread.sleep(90); // delay imitation
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}