import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RootServer {

    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket
        int port = 1111;
        DatagramSocket socket = new DatagramSocket(port);

        // RECEIVE A PACKET FROM THE CLIENT
        byte[] messageByte = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length);

        while (true) {
            socket.receive(packet);

            Thread t = new RootServerThread(socket, messageByte, packet.getAddress(), packet.getPort());
            t.start();
        }

    }
}
