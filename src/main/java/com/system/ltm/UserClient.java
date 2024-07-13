package com.system.ltm;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.*;

public class UserClient extends Application {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 4446;
    private User currentUser;
    private TextArea messageArea;
    private MulticastSocket socket;
    private InetAddress group;

    public UserClient(User user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox(10);
        messageArea = new TextArea();
        messageArea.setEditable(false);
        root.getChildren().add(messageArea);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("User Client - " + currentUser.getUsername());
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
        sendLoginInfo();
    }

    private void connectToServer() throws Exception {
        group = InetAddress.getByName(MULTICAST_ADDRESS);
        socket = new MulticastSocket(PORT);
        socket.joinGroup(group);
        new Thread(this::receiveMessages).start();
    }

    private void sendLoginInfo() {
        try {
            ComputerInfo info = ComputerInfo.getComputerInfo(currentUser.getUsername());
            String message = "LOGIN:" + new ObjectMapper().writeValueAsString(info);
            sendToServer(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                processMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("KICKED:")) {
                handleKickedMessage(message.substring(7));
            } else if (message.startsWith("ADMIN_MESSAGE:")) {
                handleAdminMessage(message.substring(14));
            } else if (message.startsWith("SHUTDOWN:")) {
                handleShutdownCommand(message.substring(9));
            } else {
                messageArea.appendText(message + "\n");
            }
        });
    }

    private void handleKickedMessage(String username) {
        if (username.equals(currentUser.getUsername())) {
            showAlert("Kicked", "You have been kicked by the admin.");
            System.exit(0);
        }
    }

    private void handleAdminMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2 && parts[0].equals(currentUser.getUsername())) {
            showAlert("Message from Admin", parts[1]);
        }
    }

    private void handleShutdownCommand(String username) {
        if (username.equals(currentUser.getUsername())) {
            showAlert("Shutdown", "Your PC will be shut down by admin request.");
            shutdownPC();
        }
    }

    private void shutdownPC() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("shutdown.exe -s -t 0");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                Runtime.getRuntime().exec("shutdown -h now");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(String message) throws Exception {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    @Override
    public void stop() throws Exception {
        if (socket != null) {
            socket.leaveGroup(group);
            socket.close();
        }
        super.stop();
    }
}