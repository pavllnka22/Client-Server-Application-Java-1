package package1;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import static org.assertj.core.api.Assertions.assertThat;

class EncrypterTest {

    private static final Encrypter SUT = new Encrypter();

    @Test
    void shouldEncryptMessage() {
        Message message = new Message((byte) 0x12, 128, 7, 333, "test message");

        byte[] resultBytes = SUT.encrypt(message);
        String hexResult = Hex.encodeHexString(resultBytes);

        System.out.println("generated: " + hexResult.toUpperCase());

        assertThat(hexResult).startsWith("13");

        assertThat(hexResult.substring(2, 4)).isEqualTo("12");
    }

    @Test
    void shouldCreateCorrectPacketStructure() {
        Message message = new Message((byte) 0x07, 11L, 2, 22, "two");

        byte[] packet = SUT.encrypt(message);

         assertThat(packet).hasSize(29);

        ByteBuffer buffer = ByteBuffer.wrap(packet);

        assertThat(buffer.get()).isEqualTo((byte) 0x13);
        assertThat(buffer.get()).isEqualTo((byte) 0x07);
        assertThat(buffer.getLong()).isEqualTo(11L);
        assertThat(buffer.getInt()).isEqualTo(11);
    }
}