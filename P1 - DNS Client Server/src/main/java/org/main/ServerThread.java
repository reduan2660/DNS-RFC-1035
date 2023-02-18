package org.main;


import org.main.endcodingDecoding.DNSresponse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    DatagramSocket socket;
    InetAddress address;
    int port;
    byte[] messageByte;

    public static String recordDir = "src/main/resources/records.txt";

    public ServerThread(DatagramSocket _socket, byte[] _messageByte, InetAddress _address, int _port) {
        this.socket = _socket;
        this.messageByte = _messageByte;
        this.address = _address;
        this.port = _port;
    }

    public static List<List<String>> searchAnswers(String domain, String domainType) {
        BufferedReader reader;
        List<List<String>> records = new ArrayList<>();


        try {
            reader = new BufferedReader(new FileReader(recordDir));
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
            reader = new BufferedReader(new FileReader(recordDir));
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
            reader = new BufferedReader(new FileReader(recordDir));
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

    public void run() {
        byte[] request = this.messageByte;

        System.out.println("id = " + DNSresponse.parseIdOf(request));
        System.out.println("isRecursive = " + DNSresponse.isRecursive(request));
        try {
            System.out.println("domain = " + DNSresponse.domain(request));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("domainType = " + DNSresponse.domainType(request));

        System.out.println("Answers: ");
        List<List<String>> answers = new ArrayList<>();
        try {
            answers = searchAnswers(DNSresponse.domain(request), DNSresponse.domainType(request));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < answers.size(); i++) {
            System.out.println(answers.get(i).toString());
        }


        System.out.println("authoritative: ");
        List<List<String>> authoritative = new ArrayList<>();
        try {
            authoritative = searchAuthoritative(DNSresponse.domain(request), DNSresponse.domainType(request));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < authoritative.size(); i++) {
            System.out.println(authoritative.get(i).toString());
        }

        System.out.println("additionals: ");
        List<List<String>> additional = new ArrayList<>();
        try {
            additional = searchAdditional(DNSresponse.domain(request), DNSresponse.domainType(request));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < additional.size(); i++) {
            System.out.println(additional.get(i).toString());
        }

        // RESPONSE
        try {
            DNSresponse dnsResponse = new DNSresponse(request, 0, answers, authoritative, additional);
            byte[] response = dnsResponse.responseMessage();

            DatagramPacket packet = new DatagramPacket(response, response.length, this.address, this.port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

