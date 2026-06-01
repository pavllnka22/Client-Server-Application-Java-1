package package1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StoreServerUDP {
    private final int port;
    private final Decrypter decrypter = new Decrypter();
    private final Encrypter encrypter = new Encrypter();

    public StoreServerUDP(int port) {
        this.port = port;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[2048];

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);

                byte[] rawData = new byte[receivePacket.getLength()];
                System.arraycopy(buffer, 0, rawData, 0, receivePacket.getLength());

                try {
                    Message request = decrypter.decrypt(rawData);
                    Message response = new Message(request.getUniqueId(), request.getMessageNumber(), request.getCommandId(), request.getSenderId(), "OK");
                    byte[] responseBytes = encrypter.encrypt(response);

                    DatagramPacket sendPacket = new DatagramPacket(
                            responseBytes, responseBytes.length,
                            receivePacket.getAddress(), receivePacket.getPort()
                    );
                    socket.send(sendPacket);
                } catch (Exception e) {
                    System.err.println( e.getMessage());
                }
            }
        } catch (StringIndexOutOfBoundsException ignored) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
