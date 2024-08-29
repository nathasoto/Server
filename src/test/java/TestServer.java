import org.example.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class TestServer {

    private static Server server;
    private Thread serverThread;
    private final int port = 6666;

    @BeforeEach
    void setUp() {

        server = new Server();
        serverThread = new Thread(() -> {
            try {
                server.start(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        server.stop();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

        @Test
    void testServerClientInteraction() throws IOException {
        // Connect to the server
        try (Socket clientSocket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            // Expect server to ask for the client's name
            String response = in.readLine();
            assertEquals("Please enter your name:", response);

            // Send name to the server
            out.println("TestClient");

            // Send a message to the server
            out.println("Hello Server");

            // Receive the same message back from the server
            response = in.readLine();
            assertEquals("Message received: Hello Server", response);
        }
    }

    @Test
    void testServerStartup() throws IOException {
        // Start server in a separate thread
        try (Socket testSocket = new Socket("localhost", port)) {
            // If we reach this point, the server is running
        }
    }
}
