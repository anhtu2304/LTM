package com.system.ltm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 4446;
    private static Map<String, ComputerInfo> onlineUsers = new ConcurrentHashMap<>();
    private static MulticastSocket socket;
    private static InetAddress group;

    public static void main(String[] args) throws Exception {
        group = InetAddress.getByName(MULTICAST_ADDRESS);
        socket = new MulticastSocket(PORT);
        socket.joinGroup(group);

        System.out.println("Multicast Server is running...");

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            processMessage(message);
        }
    }

    private static void processMessage(String message) {
        try {
            if (message.startsWith("LOGIN:")) {
                ComputerInfo info = new ObjectMapper().readValue(message.substring(6), ComputerInfo.class);
                onlineUsers.put(info.getUsername(), info);
                broadcastOnlineUsers();
            } else if (message.equals("GET_ONLINE_USERS")) {
                broadcastOnlineUsers();
            } else if (message.startsWith("KICK:")) {
                String username = message.substring(5);
                kickUser(username);
            } else if (message.startsWith("MESSAGE:")) {
                String[] parts = message.split(":", 3);
                if (parts.length == 3) {
                    sendMessageToUser(parts[1], parts[2]);
                }
            } else if (message.startsWith("SHUTDOWN:")) {
                String username = message.substring(9);
                shutdownUserPC(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void broadcastOnlineUsers() {
        try {
            String usersJson = new ObjectMapper().writeValueAsString(onlineUsers.values());
            String message = "ONLINE_USERS:" + usersJson;
            broadcastMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void kickUser(String username) {
        ComputerInfo user = onlineUsers.remove(username);
        if (user != null) {
            broadcastMessage("KICKED:" + username);
            broadcastOnlineUsers();
        }
    }

    private static void sendMessageToUser(String username, String message) {
        ComputerInfo user = onlineUsers.get(username);
        if (user != null) {
            broadcastMessage("ADMIN_MESSAGE:" + username + ":" + message);
        }
    }

    private static void shutdownUserPC(String username) {
        ComputerInfo user = onlineUsers.get(username);
        if (user != null) {
            broadcastMessage("SHUTDOWN:" + username);
        }
    }

    private static void broadcastMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}