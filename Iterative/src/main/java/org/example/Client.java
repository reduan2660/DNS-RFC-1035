package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

public class Client {
    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket
        DatagramSocket socket = new DatagramSocket();

        // Send a message to the server
        String domain = "cse.du.ac.bd"; String requestType = "A";
        DNSrequest dnSrequest = new DNSrequest(1, false, domain, requestType);
        byte[] buffer = dnSrequest.requestMessage();

        InetAddress address = InetAddress.getByName("localhost");
        int port = 1234;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);

        // Receive a response from the server
        buffer = new byte[2048];
        packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        byte[] response = packet.getData();

        // for(int i=0; i<response.length; i++) System.out.print(String.format("%8s", Integer.toBinaryString((response[i] + 256) % 256)).replace(' ', '0') + " ");
        // System.out.println();

        System.out.println("id = " + DNSrequest.parseIdOf(response));
        System.out.println("isRecursive = " + DNSresponse.isRecursive(response));
        System.out.println("domain = " + DNSresponse.domain(response));
        System.out.println("domainType = " + DNSresponse.domainType(response));

        System.out.println("ANSWERS = " + DNSrequest.parseANCOUNT(response));
        List<List<String>> answers = DNSrequest.parseAnswers(response);
        for(int i=0; i<answers.size(); i++){
            System.out.println(answers.get(i).toString());
        }

        System.out.println("AUTHORITIES = " + DNSrequest.parseNSCOUNT(response));
        List<List<String>> authorities = DNSrequest.parseAuthorities(response);
        for(int i=0; i<authorities.size(); i++){
            System.out.println(authorities.get(i).toString());
        }
        System.out.println("ADDITIONALS = " + DNSrequest.parseARCOUNT(response));
        List<List<String>> additionals = DNSrequest.parseAdditionals(response);
        for(int i=0; i<additionals.size(); i++){
            System.out.println(additionals.get(i).toString());
        }

        // Close the socket
        socket.close();
    }
}
