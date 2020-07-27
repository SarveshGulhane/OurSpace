package ourspace;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Client extends Thread {
    private DataInputStream in = null;
    private PrintStream out = null;
    private Socket clientSocket = null;
    private final Client[] threads;
    private int clientsCount;

    public Client(Socket clientSocket, Client[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        clientsCount = threads.length;
    }

    public void run() {
        int clientsCount = this.clientsCount;
        Client[] threads = this.threads;

        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new PrintStream(clientSocket.getOutputStream());

            out.println("Enter your name.");
            String name = in.readLine();

            out.println("Welcome " + name + "\n");
            synchronized (this) {
                for (int i = 0; i < clientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        break;
                    }
                }
                for (int i = 0; i < clientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].out.println(">>> " + name + " entered");
                    }
                }
            }

            while (true) {
                String line = in.readLine();
                if (line.startsWith("/q")) {
                    break;
                } else {
                    synchronized (this) {
                        for (int i = 0; i < clientsCount; i++) {
                            if (threads[i] != null && name != null) {
                                threads[i].out.println(name + "=> " + line);
                            }
                        }
                    }
                }
            }

            synchronized (this) {
                for (int i = 0; i < clientsCount; i++) {
                    if (threads[i] != null && threads[i] != this && name != null) {
                        threads[i].out.println("<<< " + name + " left");
                    }
                }
            }
            out.println("\nBye " + name);

            synchronized (this) {
                for (int i = 0; i < clientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
        }
    }
}
