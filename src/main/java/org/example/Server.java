package org.example;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Server {

    private ServerSocket serverSocket;
//    private final Set<String> uniqueIdentifiers = ConcurrentHashMap.newKeySet();
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
//            System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

            // Create a new thread to handle the client
//            new Thread(() -> handleClient(clientSocket)).start();
            // Create a new thread to handle the client
            ClientHandler clientHandler = new ClientHandler(clientSocket);
//            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }

    }

    //    private void handleClient(Socket clientSocket) {
//        String uniqueIdentifier = null;
//        String name;
//        String clientIP = clientSocket.getInetAddress().getHostAddress();
//
//        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
//
//            out.println("Please enter your name:");
//
//
//            // Loop until a unique name is chosen
//            while (true) {
//                synchronized (uniqueIdentifiers) {
//                    name = in.readLine();
//                    // Combine IP address and name
//                    uniqueIdentifier = name + "_" + clientIP;
//
//
//                    if (name != null && !name.isEmpty() && !uniqueIdentifiers.contains(uniqueIdentifier)) {
//                        uniqueIdentifiers.add(uniqueIdentifier);
//                        out.println("Welcome " + name + "! You have joined the chat.");
//                        System.out.println("Client connected: " + uniqueIdentifier);
//                        break;
//                    } else if (uniqueIdentifiers.contains(uniqueIdentifier)) {
//                        out.println("Name already taken with your IP. Please choose a different name.");
//                    } else {
//                        out.println("Name provided is null or empty. Please choose a different name.");
//                    }
//                }
//            }
//
//            // Continuously listen for messages from the client
//            String message;
//            while ((message = in.readLine()) != null) {
//                System.out.println("Received message from " + uniqueIdentifier + ": " + message);
//                // Echo the message back to the client
//                out.println("Message received: " + message);
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error handling client: " + e.getMessage());
//        } finally {
//            // Remove the unique identifier when the client disconnects
//            if (uniqueIdentifier != null) {
//                synchronized (uniqueIdentifiers) {
//                    uniqueIdentifiers.remove(uniqueIdentifier);
//                }
//                System.out.println("Client " + uniqueIdentifier + " disconnected.");
//            }
//        }
//    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;
        private String uniqueIdentifier;
        private String clientIP;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIP = clientSocket.getInetAddress().getHostAddress();

                // Solicita y valida el nombre del cliente
                out.println("Please enter your name:");
                while (true) {
                    synchronized (clients) {
                        name = in.readLine();
                        if (name == null) {
                            // Manejo de desconexión inesperada
                            return;
                        }
                        uniqueIdentifier = name + "_" + clientIP;

                        // Verificar el identificador único
                        boolean nameTaken = clients.stream().anyMatch(c -> c.uniqueIdentifier.equals(uniqueIdentifier));
                        if (!nameTaken && !name.isEmpty()) {
                            clients.add(this);
                            out.println("Welcome " + name + "! You have joined the chat.");
                            System.out.println("Client connected: " + uniqueIdentifier);
                            break;
                        } else if (nameTaken) {
                            out.println("Name already taken with your IP. Please choose a different name.");
                        } else {
                            out.println("Name provided is null or empty. Please choose a different name.");
                        }
                    }
                }


                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received message from " + uniqueIdentifier + ": " + message);
                    broadcastMessage(uniqueIdentifier, message);
                }

            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                clients.remove(this);
                if (uniqueIdentifier != null) {
                    System.out.println("Client " + uniqueIdentifier + " disconnected.");
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void broadcastMessage(String sender, String message) {
            for (ClientHandler client : clients) {
                if (!client.uniqueIdentifier.equals(sender)) {
                    client.out.println(sender + ": " + message);
                }
            }
        }
    }

    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start(6666);
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}


