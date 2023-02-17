package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ISPserver {

    public static List<List<String>> searchAnswers(String domain, String domainType) {
        BufferedReader reader;
        List<List<String>> records = new ArrayList<>();


        try {
            reader = new BufferedReader(new FileReader("src/main/java/org/example/records.txt"));
            String line = reader.readLine();
            line = reader.readLine();

            while (line != null) {
                String[] record = line.split("\\s+");

                if(record[0].equals(domain) && record[record.length - 2].equals(domainType)){
                    List<String> validRecord = new ArrayList<>();
                    validRecord.add(record[record.length - 3]); // VALUE
                    validRecord.add(record[record.length - 2]); // TYPE
                    validRecord.add(record[record.length - 1]); // TTL

                    records.add(validRecord);
                }
                // read next line
                line = reader.readLine();
            }

            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
    public static List<List<String>> searchAuthoritative(String domain, String domainType) {
        BufferedReader reader;
        List<List<String>> records = new ArrayList<>();


        try {
            reader = new BufferedReader(new FileReader("src/main/java/org/example/records.txt"));
            String line = reader.readLine();
            line = reader.readLine();

            while (line != null) {
                String[] record = line.split("\\s+");

                if(record[0].equals(domain) && record[record.length - 2].equals("NS")){
                    List<String> validRecord = new ArrayList<>();
                    validRecord.add(record[record.length - 3]); // VALUE
                    validRecord.add(record[record.length - 2]); // TYPE
                    validRecord.add(record[record.length - 1]); // TTL

                    records.add(validRecord);
                }
                // read next line
                line = reader.readLine();
            }

            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
    public static List<List<String>> searchAdditional(String domain, String domainType) {
        BufferedReader reader;
        List<List<String>> records = new ArrayList<>();


        try {
            reader = new BufferedReader(new FileReader("src/main/java/org/example/records.txt"));
            String line = reader.readLine();
            line = reader.readLine();

            while (line != null) {
                String[] record = line.split("\\s+");

                if(record[0].equals(domain) && !record[record.length - 2].equals("NS") && !record[record.length - 3].equals(domainType)){
                    List<String> validRecord = new ArrayList<>();
                    validRecord.add(record[record.length - 3]); // VALUE
                    validRecord.add(record[record.length - 2]); // TYPE
                    validRecord.add(record[record.length - 1]); // TTL

                    records.add(validRecord);
                }
                // read next line
                line = reader.readLine();
            }

            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }




    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket
        int port = 1234;
        DatagramSocket socket = new DatagramSocket(port);

        // Receive a message from the client
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        
        byte[] request = packet.getData();

        System.out.println("id = " + DNSresponse.id(request));
        System.out.println("isRecursive = " + DNSresponse.isRecursive(request));
        System.out.println("domain = " + DNSresponse.domain(request));
        System.out.println("domainType = " + DNSresponse.domainType(request));

        System.out.println("Answers: ");
        List<List<String>> answers = searchAnswers(DNSresponse.domain(request), DNSresponse.domainType(request));
        for(int i=0; i<answers.size(); i++){
            System.out.println(answers.get(i).toString());
        }

        System.out.println("authoritative: ");
        List<List<String>> authoritative = searchAuthoritative(DNSresponse.domain(request), DNSresponse.domainType(request));
        for(int i=0; i<authoritative.size(); i++){
            System.out.println(authoritative.get(i).toString());
        }

        System.out.println("additionals: ");
        List<List<String>> additional = searchAdditional(DNSresponse.domain(request), DNSresponse.domainType(request));
        for(int i=0; i<additional.size(); i++){
            System.out.println(additional.get(i).toString());
        }

        // Send a response to the client
        String response = "Welcome to CSE 3111!";
        buffer = response.getBytes();
        packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
        socket.send(packet);

        // Close the socket
        socket.close();
    }
}
