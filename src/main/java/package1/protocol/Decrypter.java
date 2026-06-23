package package1.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Decrypter {

    public Message decrypt(byte[] message) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(message);

        byte magicByte  = byteBuffer.get();
        if(magicByte != 0x13) {
            throw new IllegalArgumentException("invalid magic byte");
        }
        byte uniqueId = byteBuffer.get();
        long messageNumber = byteBuffer.getLong();
        int wLen = byteBuffer.getInt();
        short firstCrc = byteBuffer.getShort();

        short expectedSum = Crc16.calculateCrc(message, 0, 14);
        if (firstCrc != expectedSum) {
            throw new SecurityException("header crc mismatch");
        }

        int commandId = byteBuffer.getInt();
        int senderId = byteBuffer.getInt();

        int textLength = wLen - 8;
        byte[] encryptedPayload = new byte[textLength];
        byteBuffer.get(encryptedPayload);

        short secondCrc = byteBuffer.getShort();

        short expectedSum2 = Crc16.calculateCrc(message, 16, wLen);
        if (secondCrc != expectedSum2) {
            throw new SecurityException("body crc mismatch");
        }

        byte[] decrypted = new byte[encryptedPayload.length];
        for (int i = 0; i < encryptedPayload.length; i++) {
            decrypted[i] = (byte) (encryptedPayload[i] ^ 22);
        }

        String decryptedText = new String(decrypted, StandardCharsets.UTF_8);

        return new Message(uniqueId, messageNumber, commandId, senderId, decryptedText);
    }
}