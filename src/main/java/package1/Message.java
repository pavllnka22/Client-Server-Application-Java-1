package package1;


public class Message {
    private byte uniqueId;
    private long messageNumber;
    private int commandId;
    private int senderId;
    private String encryptedMessage;

    public Message() {}

    public Message(byte uniqueId, long messageNumber, int commandId, int senderId, String encryptedMessage) {
        this.uniqueId = uniqueId;
        this.messageNumber = messageNumber;
        this.commandId = commandId;
        this.senderId = senderId;
        this.encryptedMessage = encryptedMessage;
    }

    public byte getUniqueId() { return uniqueId; }
    public void setUniqueId(byte uniqueId) { this.uniqueId = uniqueId; }
    public long getMessageNumber() { return messageNumber; }
    public void setMessageNumber(long messageNumber) { this.messageNumber = messageNumber; }
    public int getCommandId() { return commandId; }
    public void setCommandId(int commandId) { this.commandId = commandId; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public String getEncryptedMessage() { return encryptedMessage; }
    public void setEncryptedMessage(String encryptedMessage) { this.encryptedMessage = encryptedMessage; }
}