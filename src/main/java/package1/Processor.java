package package1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private final BlockingQueue<Message> inputQueue;
    private final BlockingQueue<Message> outputQueue;

    private final StoreServices storeServices;

    final ObjectMapper objectMapper = new ObjectMapper();

    public Processor(BlockingQueue<Message> inputQueue, BlockingQueue<Message> outputQueue, StoreServices storeServices) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.storeServices = storeServices;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Message request = inputQueue.take();

                CommandType type = CommandType.fromId(request.getCommandId());
                String responseText = "OK";
                switch (type) {
                    case GET_QUANTITY :
                        String productId = request.getEncryptedMessage();
                        responseText = storeServices.read(productId)
                                .map(p -> "Quantity: " + p.getQuantity())
                                .orElse("Product was not found");
                        break;
                    case DELETE_ITEMS:
                        responseText ="Delete items";
                    case ADD_ITEMS :
                        Product newProduct = objectMapper.readValue(request.getEncryptedMessage(), Product.class);
                        storeServices.create(newProduct);
                        responseText = "Product created successfully";
                        break;
                    case ADD_GROUP:
                        responseText = "Adding group of items";
                    case ADD_ITEMS_TO_GROUP:
                        responseText = "Adding items to group";
                    case SET_PRICE:
                        responseText = "Setting price";

                }

                Message response = new Message(
                        request.getUniqueId(),
                        request.getMessageNumber(),
                        request.getCommandId(),
                        request.getSenderId(),
                        responseText
                );
                outputQueue.put(response);
            }
        } catch (InterruptedException | JsonProcessingException e) {
            Thread.currentThread().interrupt();
        }
    }
}
