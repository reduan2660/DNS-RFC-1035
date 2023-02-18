import org.main.endcodingDecoding.DNSrequest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TLDServer {

    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket
        int port = 1112;
        DatagramSocket socket = new DatagramSocket(port);

        // RECEIVE A PACKET FROM THE CLIENT
        byte[] messageByte = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length);

        while (true) {
            if(!DNSrequest.isResponse(messageByte)) {
                System.out.println("REQUEST RECEIVED from " + packet.getAddress() + "-----------------------");
                socket.receive(packet);
                Thread t = new TLDServerThread(socket, messageByte, packet.getAddress(), packet.getPort());
                t.start();
            }
        }

    }
}