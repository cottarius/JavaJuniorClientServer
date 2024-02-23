package chat.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(4300);
        Server server = new Server(serverSocket);
        server.runServer();
    }

}
