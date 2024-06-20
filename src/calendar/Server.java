package calendar;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a server for managing calendar events.
 */
public class Server {

    private static final int port = 2020;
    private ServerSocket serverSocket;
    private Map<LocalDate, ArrayList<String>> events;

    /**
     * Constructor of Server class.
     */
    public Server() {
        try {
            serverSocket = new ServerSocket(port);
            events = new HashMap<>();
            loadEventsFromFile();
            System.out.println("Server is running");
        } catch (IOException e) {
            System.out.println("Socket creation failed");
            System.exit(1);
        }
    }

    /**
     * Starts the server and listens for client connections.
     *
     * @throws IOException if an I/O error occurs while waiting for a connection
     */
    public void work() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientService service = new ClientService(clientSocket);
            service.start();
        }
    }

    /**
     * Loads events from a file into memory.
     */
    private void loadEventsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("events.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                LocalDate date = LocalDate.parse(parts[0]);
                String event = parts[1];
                events.computeIfAbsent(date, item -> new ArrayList<>()).add(event);
            }
        } catch (IOException e) {
            System.out.println("Failed to load events from file");
        }
    }

    /**
     * Saves events from memory to a file.
     */
    private void saveEventsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("events.txt"))) {
            for (Map.Entry<LocalDate, ArrayList<String>> eventItem : events.entrySet()) {
                for (String event : eventItem.getValue()) {
                    writer.println(eventItem.getKey() + ";" + event);
                }
            }
            System.out.println("Events have been saved to file");
        } catch (IOException e) {
            System.out.println("Failed to save events to file");
        }
    }

    /**
     * Represents a thread to handle client connections and manage events.
     */
    private class ClientService extends Thread {

        private Socket socket;

        /**
         * Constructs a new ClientService instance.
         *
         * @param socket the client socket
         */
        public ClientService(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                for (Map.Entry<LocalDate, ArrayList<String>> entry : events.entrySet()) {
                    for (String event : entry.getValue()) {
                        out.println(entry.getKey() + ";" + event);
                    }
                }

                out.println("END");

                String message;
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(";");
                    LocalDate date = LocalDate.parse(parts[0]);
                    String event = parts[1];
                    events.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Client service error");
            } finally {
                saveEventsToFile();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.work();
    }

}
