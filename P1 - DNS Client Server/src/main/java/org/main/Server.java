package org.main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket
        int port = 1234;
        DatagramSocket socket = new DatagramSocket(port);

        // RECEIVE A PACKET FROM THE CLIENT
        byte[] messageByte = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length);

        while (true) {
            socket.receive(packet);

            Thread t = new ServerThread(socket, messageByte, packet.getAddress(), packet.getPort());
            t.start();
        }

    }
}
