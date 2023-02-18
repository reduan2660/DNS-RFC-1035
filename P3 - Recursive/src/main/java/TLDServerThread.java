import org.main.endcodingDecoding.DNSrequest;
import org.main.endcodingDecoding.DNSresponse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TLDServerThread extends Thread {
    DatagramSocket socket;
    InetAddress address;
    int port;
    byte[] messageByte;

    public static String recordDir = "src/main/resources/TLDServerRecords.txt";

    public TLDServerThread(DatagramSocket _socket, byte[] _messageByte, InetAddress _address, int _port) {
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
        try {
            if(!DNSrequest.isResponse(request)) {
                /* SEARCH IN LOCAL RECORDS */
                System.out.println("Request received : " + address + ":" + port);
                System.out.println(DNSresponse.parseIdOf(request) + " " + DNSresponse.domain(request) + " " + DNSresponse.domainType(request));

                List<List<String>> answers = searchAnswers(DNSresponse.domain(request), DNSresponse.domainType(request));
                System.out.println("Answers: " + answers.size());
                for (int i = 0; i < answers.size(); i++) System.out.println(answers.get(i).toString());

                List<List<String>> authoritative = searchAuthoritative(DNSresponse.domain(request), DNSresponse.domainType(request));
                System.out.println("authoritative: " + authoritative.size());
                for (int i = 0; i < authoritative.size(); i++) System.out.println(authoritative.get(i).toString());

                List<List<String>> additional = searchAdditional(DNSresponse.domain(request), DNSresponse.domainType(request));
                System.out.println("additionals: " + additional.size());
                for (int i = 0; i < additional.size(); i++) System.out.println(additional.get(i).toString());

                if (answers.size() > 0) { // Local Server contains answers
                    try {
                        System.out.println("RESPONSE FROM LOCALSERVER");
                        DNSresponse dnsResponse = new DNSresponse(request, 0, answers, authoritative, additional);
                        byte[] response = dnsResponse.responseMessage();

                        DatagramPacket packet = new DatagramPacket(response, response.length, this.address, this.port);
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                /* Try nameservers */
                for (int i = 0; i < authoritative.size(); i++) {
                    String nsDomain = authoritative.get(i).get(i).split(":")[0];
                    int nsPort = Integer.parseInt(authoritative.get(i).get(i).split(":")[1]);

                    InetAddress address = InetAddress.getByName(nsDomain);
                    int port = nsPort; // Name Server Port
                    DNSrequest dnSrequest = new DNSrequest(DNSresponse.parseIdOf(request), DNSresponse.isRecursive(request), DNSresponse.domain(request), DNSresponse.domainType(request));
                    byte[] buffer = dnSrequest.requestMessage();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(packet);

                    System.out.println("Request sent to Name Server");

                    // Receive a response from the server
//                    buffer = new byte[2048];
//                    packet = new DatagramPacket(buffer, buffer.length);
//                    socket.receive(packet);
//                    byte[] response = packet.getData();

                }
            }
            else{
                byte[] response = request;
                List<List<String>> authAnswers = DNSrequest.parseAnswers(response);
                System.out.println("Answers from auth: " + authAnswers.size());
                for (int j = 0; j < authAnswers.size(); j++) System.out.println(authAnswers.get(j).toString());

                List<List<String>> authAuthoritative = DNSrequest.parseAuthorities(response);
                System.out.println("authoritative from auth: " + authAuthoritative.size());
                for (int j = 0; j < authAuthoritative.size(); j++)
                    System.out.println(authAuthoritative.get(j).toString());

                List<List<String>> authAdditional = DNSrequest.parseAdditionals(response);
                System.out.println("additionals from auth: " + authAdditional.size());
                for (int j = 0; j < authAdditional.size(); j++)
                    System.out.println(authAdditional.get(j).toString());

                if (authAnswers.size() > 0) {
                    System.out.println("RESPONSE FROM AUTH SERVER");
                    DNSresponse dnsResponse = new DNSresponse(request, 0, authAnswers, authAuthoritative, authAdditional);
                    byte[] rootResponse = dnsResponse.responseMessage();
                    int rootPort = 1111;
                    System.out.println("SENDING RESPONSE TO " + this.address + ":" + rootPort );
                    DatagramPacket packet = new DatagramPacket(rootResponse, rootResponse.length, this.address, rootPort);
                    socket.send(packet);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

