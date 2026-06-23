package package1.client;

import package1.protocol.Decrypter;
import package1.protocol.Encrypter;
import package1.protocol.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class StoreClientTCP {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final Encrypter encrypter = new Encrypter();
    private final Decrypter decrypter = new Decrypter();

    public StoreClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void connect() {
        while (socket == null || socket.isClosed()) {
            try {
                this.socket = new Socket(host, port);
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());

            } catch (IOException e) {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public Message sendRequest(Message message) {
        while (true) {
            try {
                connect();

                byte[] packetBytes = encrypter.encrypt(message);
                out.write(packetBytes);
                out.flush();

                byte[] header = new byte[16];
                in.readFully(header);
                int wLen = ((header[10] & 0xFF) << 24) | ((header[11] & 0xFF) << 16) |
                        ((header[12] & 0xFF) << 8)  | (header[13] & 0xFF);

                byte[] remainder = new byte[wLen + 2];
                in.readFully(remainder);

                byte[] fullResponse = new byte[16 + remainder.length];
                System.arraycopy(header, 0, fullResponse, 0, 16);
                System.arraycopy(remainder, 0, fullResponse, 16, remainder.length);

                return decrypter.decrypt(fullResponse);

            } catch (IOException e) {
                closeSilently();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }

    private void closeSilently() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        socket = null;
    }
}