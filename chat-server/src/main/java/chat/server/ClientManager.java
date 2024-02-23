package chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ClientManager implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clients.add(this);
            //TODO: ...
            name = bufferedReader.readLine();
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEveryThing(socket, bufferedReader, bufferedWriter);
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Удаление клиента из коллекции. Заврешение работы socket, bufferedReader, bufferedWriter
     * @param socket соединение с клиентским сокетом
     * @param bufferedReader буфер на чтение данных
     * @param bufferedWriter буфер для записи данных
     */
    private void closeEveryThing(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Удаление клиента из коллекции
     */
    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }

    /**
     * Отправка сообщения одному слушателю
     * @param message сообщение
     */
    private void personalMessage(String message) {
        String[] arr = message.split(" ", 3);
        String name = arr[1].substring(1);
        String splitMessage = arr[2];
        try {
            ClientManager client = clients.stream().filter(entry -> entry.getName().equalsIgnoreCase(name)).findFirst().get();
            client.bufferedWriter.write("private from " + getName() + ": " + splitMessage);
            client.bufferedWriter.newLine();
            client.bufferedWriter.flush();
        } catch (NoSuchElementException e) {
            try {
                ClientManager clientManager = this;
                clientManager.bufferedWriter.write("Клиента с таким именем нет.");
                clientManager.bufferedWriter.newLine();
                clientManager.bufferedWriter.flush();
            } catch (Exception ex) {
                closeEveryThing(socket, bufferedReader, bufferedWriter);
            }
        } catch (Exception e) {
            closeEveryThing(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Отправка сообщения всем слушателям
     * @param message сообщение
     */
    private void broadcastMessage(String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.equals(this) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (Exception e) {
                closeEveryThing(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (!socket.isClosed()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient.contains(String.valueOf('@'))) {
                    personalMessage(messageFromClient);
                } else {
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEveryThing(socket, bufferedReader, bufferedWriter);
            }
        }
    }
}
