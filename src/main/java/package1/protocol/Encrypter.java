package package1.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Encrypter {

    public byte[] encrypt(Message message) {

        byte[] actualMessage = message.getEncryptedMessage().getBytes(StandardCharsets.UTF_8);

        byte[] encryptedMessage = new byte[actualMessage.length];
        for (int i = 0; i < actualMessage.length; i++) {
            encryptedMessage[i] = (byte) (actualMessage[i] ^ 22);
        }

        int wLen = 4 + 4 + encryptedMessage.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(16 + wLen + 2);

        byteBuffer.put((byte) 0x13);
        byteBuffer.put(message.getUniqueId());
        byteBuffer.putLong(message.getMessageNumber());
        byteBuffer.putInt(wLen);

        short crc1 = Crc16.calculateCrc(byteBuffer.array(), 0, 14);
        byteBuffer.putShort(crc1);

        byteBuffer.putInt(message.getCommandId());
        byteBuffer.putInt(message.getSenderId());
        byteBuffer.put(encryptedMessage);

        short crc2 = Crc16.calculateCrc(byteBuffer.array(), 16, wLen);
        byteBuffer.putShort(crc2);

        return byteBuffer.array();
    }
}