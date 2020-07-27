package ourspace;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import javax.swing.*;

public class Server {
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private static int maxClient = 4;
	private static final Client[] threads = new Client[maxClient];

	public static void main(String[] args) {
		try {

			Object[] selectValues = { "Server", "Client" };
			Object selection = JOptionPane.showInputDialog(null, "Login as : ", "OurSpace",
					JOptionPane.QUESTION_MESSAGE, null, selectValues, "Server");

			if (selection.equals("Server")) {
				serverSocket = new ServerSocket(4321);
				System.out.println("\nServer Started...");

				while (true) {
					int i;
					clientSocket = serverSocket.accept();
					for (i = 0; i < maxClient; i++) {
						if (threads[i] == null) {
							(threads[i] = new Client(clientSocket, threads)).start();
							break;
						}
					}
				}
			}

			if (selection.equals("Client")) {
				new Chat().main();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
