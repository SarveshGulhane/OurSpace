package ourspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observer;
import java.util.Observable;

public class Chat {
    static class Access extends Observable {
        private Socket socket;
        private OutputStream out;

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        public void InitSocket(String server, int port) throws IOException {
            socket = new Socket(server, port);
            out = socket.getOutputStream();

            Thread receivingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null)
                            notifyObservers(line);
                    } catch (IOException ex) {
                        notifyObservers(ex);
                    }
                }
            };
            receivingThread.start();
        }

        public void send(String text) {
            try {
                out.write((text + "\n").getBytes());
                out.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    static class Frame extends JFrame implements Observer {
        private static final long serialVersionUID = 1L;
        private JTextArea textArea;
        private JTextField textField;
        private JButton sendButton;
        private Access chatAccess;

        public Frame(Access chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            GUI();
        }

        private void GUI() {
            textArea = new JTextArea(20, 40);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            textField = new JTextField();
            sendButton = new JButton("Send");
            box.add(textField);
            box.add(sendButton);

            ActionListener sendListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String str = textField.getText();
                    if (str != null && str.trim().length() > 0)
                        chatAccess.send(str);
                    textField.selectAll();
                    textField.requestFocus();
                    textField.setText("");
                }
            };
            textField.addActionListener(sendListener);
            sendButton.addActionListener(sendListener);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.close();
                }
            });
        }

        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textArea.append(finalArg.toString());
                    textArea.append("\n");
                }
            });
        }
    }

    public void main() {
        Access access = new Access();
        JFrame frame = new Frame(access);

        frame.setTitle("OurSpace");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);

        try {
            access.InitSocket("localhost", 4321);
        } catch (IOException ex) {
            System.out.println("Cannot connect to Server!");
            ex.printStackTrace();
            System.exit(0);
        }
    }
}