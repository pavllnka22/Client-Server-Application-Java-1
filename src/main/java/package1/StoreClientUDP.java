package package1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class StoreClientUDP {
    private final String host;
    private final int port;
    private final Encrypter encrypter = new Encrypter();
    private final Decrypter decrypter = new Decrypter();
    private static final int MAX_TRIES = 3;
    private static final int TIME_LIMIT = 2200;

    public StoreClientUDP(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Message sendRequestWithRetry(Message message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIME_LIMIT);

            byte[] packetBytes = encrypter.encrypt(message);
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket sendPacket = new DatagramPacket(packetBytes, packetBytes.length, address, port);

            byte[] responseBuffer = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

            int attempts = 0;
            while (attempts < MAX_TRIES) {
                try {
                    attempts++;
                    socket.send(sendPacket);
                    socket.receive(receivePacket);

                    byte[] actualData = new byte[receivePacket.getLength()];
                    System.arraycopy(responseBuffer, 0, actualData, 0, receivePacket.getLength());
                    return decrypter.decrypt(actualData);

                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
