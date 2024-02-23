package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    /**
     * Серверный сокет
     */
    private final ServerSocket serverSocket;

    /**
     * Инициализация серверного сокета
     * @param serverSocket серверный сокет
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void runServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Подключен новый клиент!");
                ClientManager clientManager = new ClientManager(socket);
                Thread thread = new Thread(clientManager);
                thread.start();
            }
        } catch (Exception e) {
            closeSocket();
        }

    }

    private void closeSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
