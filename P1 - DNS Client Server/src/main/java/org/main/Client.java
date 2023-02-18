package org.main;


import org.main.endcodingDecoding.DNSrequest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        // Scanner
        Scanner inputStream = new Scanner(System.in);

        // Create a DatagramSocket
        DatagramSocket socket = new DatagramSocket();

        // Send a message to the server
        String domain = inputStream.nextLine();
        String domainType = inputStream.nextLine();
        Boolean recursive = false;


        DNSrequest dnSrequest = new DNSrequest(1, recursive, domain, domainType);
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

