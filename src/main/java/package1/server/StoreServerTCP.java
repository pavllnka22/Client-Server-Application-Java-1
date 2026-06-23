package package1.server;

import package1.protocol.Message;
import package1.protocol.Decrypter;
import package1.protocol.Encrypter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreServerTCP {
    private final int port;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Decrypter decrypter = new Decrypter();
    private final Encrypter encrypter = new Encrypter();

    public StoreServerTCP(int port) { this.port = port; }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println( e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            while (!socket.isClosed()) {
                byte[] header = new byte[16];
                in.readFully(header);

                int wLen = ((header[10] & 0xFF) << 24) | ((header[11] & 0xFF) << 16) |
                        ((header[12] & 0xFF) << 8)  | (header[13] & 0xFF);

                byte[] remainder = new byte[wLen + 2];
                in.readFully(remainder);

                byte[] fullPacket = new byte[16 + remainder.length];
                System.arraycopy(header, 0, fullPacket, 0, 16);
                System.arraycopy(remainder, 0, fullPacket, 16, remainder.length);

                Message request = decrypter.decrypt(fullPacket);

                Message response = new Message(request.getUniqueId(), request.getMessageNumber(), request.getCommandId(), request.getSenderId(), "OK");
                byte[] responseBytes = encrypter.encrypt(response);

                out.write(responseBytes);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
