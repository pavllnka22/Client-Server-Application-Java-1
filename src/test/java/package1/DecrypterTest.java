package package1;


import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecrypterTest {

    private static final Decrypter SUT = new Decrypter();

       private static final String VALID_HEX = "1312000000000000008000000014D02A000000070000014D62736562367B73656577717322E5";

    @Test
    void shouldDecryptMessageSuccessfully() throws DecoderException {
        byte[] packetBytes = Hex.decodeHex(VALID_HEX);
        Message actual = SUT.decrypt(packetBytes);

        assertThat(actual)
                .returns((byte) 0x12, Message::getUniqueId)
                .returns(128L, Message::getMessageNumber)
                .returns(7, Message::getCommandId)
                .returns(333, Message::getSenderId)
                .returns("test message", Message::getEncryptedMessage); // Текст успішно розшифровано!
    }

    @Test
    void shouldThrowExceptionWhenHeaderIsCorrupted() throws DecoderException {
        byte[] packetBytes = Hex.decodeHex(VALID_HEX);

        packetBytes[1] = (byte) (packetBytes[1] ^ 0xFF);

        assertThrows(SecurityException.class, () -> SUT.decrypt(packetBytes));
    }

    @Test
    void shouldThrowExceptionWhenBodyIsCorrupted() throws DecoderException {
        byte[] packetBytes = Hex.decodeHex(VALID_HEX);

        packetBytes[16] = (byte) (packetBytes[16] ^ 55);

        assertThrows(SecurityException.class, () -> SUT.decrypt(packetBytes));
    }
}