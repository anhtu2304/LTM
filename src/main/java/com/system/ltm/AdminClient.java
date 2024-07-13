package com.system.ltm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.*;
import java.util.List;

public class AdminClient extends Application {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 4446;
    private User currentUser;
    private ListView<ComputerInfo> userListView;
    private TextArea messageArea;
    private MulticastSocket socket;
    private InetAddress group;

    public AdminClient(User user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        userListView = new ListView<>();
        messageArea = new TextArea();
        messageArea.setEditable(false);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshUserList());

        Button kickButton = new Button("Kick User");
        kickButton.setOnAction(e -> kickSelectedUser());

        Button messageButton = new Button("Send Message");
        messageButton.setOnAction(e -> sendMessageToUser());

        Button shutdownButton = new Button("Shutdown PC");
        shutdownButton.setOnAction(e -> shutdownSelectedPC());

        HBox buttonBox = new HBox(10, refreshButton, kickButton, messageButton, shutdownButton);

        root.getChildren().addAll(userListView, buttonBox, messageArea);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Admin Client - " + currentUser.getUsername());
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
        refreshUserList();
    }

    private void connectToServer() throws Exception {
        group = InetAddress.getByName(MULTICAST_ADDRESS);
        socket = new MulticastSocket(PORT);
        socket.joinGroup(group);
        new Thread(this::receiveMessages).start();
    }

    private void refreshUserList() {
        try {
            sendToServer("GET_ONLINE_USERS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void kickSelectedUser() {
        ComputerInfo selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                sendToServer("KICK:" + selectedUser.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageToUser() {
        ComputerInfo selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Send Message");
            dialog.setHeaderText("Send a message to " + selectedUser.getUsername());
            dialog.setContentText("Message:");

            dialog.showAndWait().ifPresent(message -> {
                try {
                    sendToServer("MESSAGE:" + selectedUser.getUsername() + ":" + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void shutdownSelectedPC() {
        ComputerInfo selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                sendToServer("SHUTDOWN:" + selectedUser.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if (message.startsWith("ONLINE_USERS:")) {
                updateUserList(message.substring(13));
            } else {
                messageArea.appendText(message + "\n");
            }
        });
    }

    private void updateUserList(String usersJson) {
        try {
            List<ComputerInfo> users = new ObjectMapper().readValue(usersJson, new TypeReference<List<ComputerInfo>>() {});
            ObservableList<ComputerInfo> observableUsers = FXCollections.observableArrayList(users);
            userListView.setItems(observableUsers);
            userListView.setCellFactory(lv -> new ListCell<ComputerInfo>() {
                @Override
                protected void updateItem(ComputerInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(String message) throws Exception {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
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