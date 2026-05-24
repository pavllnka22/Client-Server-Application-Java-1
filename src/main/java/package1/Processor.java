package package1;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private final BlockingQueue<Message> inputQueue;
    private final BlockingQueue<Message> outputQueue;

    public Processor(BlockingQueue<Message> inputQueue, BlockingQueue<Message> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Message request = inputQueue.take();

                CommandType type = CommandType.fromId(request.getCommandId());
                switch (type) {
                    case GET_QUANTITY -> System.out.println("Item quantity check");
                    case DELETE_ITEMS -> System.out.println("Delete items");
                    case ADD_ITEMS -> System.out.println("Adding items");
                    case ADD_GROUP -> System.out.println("Adding group of items");
                    case ADD_ITEMS_TO_GROUP -> System.out.println("Adding items to group");
                    case SET_PRICE -> System.out.println("Setting price");
                }

                Message response = new Message(
                        request.getUniqueId(),
                        request.getMessageNumber(),
                        request.getCommandId(),
                        request.getSenderId(),
                        "ok"
                );
                outputQueue.put(response);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
