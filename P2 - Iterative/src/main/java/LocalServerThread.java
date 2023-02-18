import org.main.endcodingDecoding.DNSrequest;
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

public class LocalServerThread extends Thread {
    DatagramSocket socket;
    InetAddress address;
    int port;
    byte[] messageByte;

    public static String recordDir = "src/main/resources/LocalServerRecords.txt";

    public LocalServerThread(DatagramSocket _socket, byte[] _messageByte, InetAddress _address, int _port) {
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
            System.out.println("Request received : " +  address + ":" + port);
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


            /* Iterations */
            if(answers.size() > 0) { // Local Server contains answers

                try {
                    System.out.println("RESPONSE FROM LOCALSERVER");
                    DNSresponse dnsResponse = new DNSresponse(request, 0, answers, authoritative, additional);
                    byte[] response = dnsResponse.responseMessage();

                    DatagramPacket packet = new DatagramPacket(response, response.length, this.address, this.port);
                    socket.send(packet);
                } catch (IOException e) {e.printStackTrace();}
                return;
            }

            try {
                // ASK ROOT SERVER
                System.out.println("SENDING REQUEST TO ROOT SERVER");
                System.out.println(DNSresponse.parseIdOf(request) + " " + DNSresponse.domain(request) + " " + DNSresponse.domainType(request));

                DNSrequest dnsRequestToRoot = new DNSrequest(DNSresponse.parseIdOf(request), false, DNSresponse.domain(request), DNSresponse.domainType(request));
                byte[] buffer = dnsRequestToRoot.requestMessage();

                InetAddress address = InetAddress.getByName("localhost");
                int port = 1111; // Root Server
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);

                buffer = new byte[2048];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] rootResponse = packet.getData();
                List<List<String>> rootAnswers = DNSrequest.parseAnswers(rootResponse);
                List<List<String>> rootAuthoritatives = DNSrequest.parseAuthorities(rootResponse);

                /* ROOT SERVER GOT ANSWER */
                if(rootAnswers.size() > 0){
                    // RESPONSE
                    try {
                        DNSresponse dnsResponse = new DNSresponse(request, 0, rootAnswers, rootAuthoritatives, additional);
                        rootResponse = dnsResponse.responseMessage();

                        DatagramPacket rootPacket = new DatagramPacket(rootResponse, rootResponse.length, this.address, this.port);
                        socket.send(rootPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return;
                }

                /* ROOT SERVER REFERRED TO TLD */
                // ASK TLD SERVER
                System.out.println("SENDING REQUEST TO TLD SERVER");
                System.out.println(DNSresponse.parseIdOf(request) + " " + DNSresponse.domain(request) + " " + DNSresponse.domainType(request));

                DNSrequest dnsRequestToTLD = new DNSrequest(DNSresponse.parseIdOf(request), false, DNSresponse.domain(request), DNSresponse.domainType(request));
                buffer = dnsRequestToTLD.requestMessage();

                address = InetAddress.getByName("localhost");
                port = 1112; // TLD Server
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);

                buffer = new byte[2048];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] tldResponse = packet.getData();
                List<List<String>> tldAnswers = DNSrequest.parseAnswers(tldResponse);
                List<List<String>> tldAuthoritatives = DNSrequest.parseAuthorities(tldResponse);

                /* TLD SERVER GOT ANSWER */
                if(tldAnswers.size() > 0){
                    // RESPONSE
                    try {
                        DNSresponse dnsResponse = new DNSresponse(request, 0, tldAnswers, tldAuthoritatives, additional);
                        rootResponse = dnsResponse.responseMessage();

                        DatagramPacket rootPacket = new DatagramPacket(rootResponse, rootResponse.length, this.address, this.port);
                        socket.send(rootPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return;
                }

                /* TLD SERVER REFERRED TO AUTH */
                // ASK AUTH SERVER
                System.out.println("SENDING REQUEST TO AUTH SERVER");
                System.out.println(DNSresponse.parseIdOf(request) + " " + DNSresponse.domain(request) + " " + DNSresponse.domainType(request));

                DNSrequest dnsRequestToAuth = new DNSrequest(DNSresponse.parseIdOf(request), false, DNSresponse.domain(request), DNSresponse.domainType(request));
                buffer = dnsRequestToAuth.requestMessage();

                address = InetAddress.getByName("localhost");
                port = 1113; // Auth Server
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);

                buffer = new byte[2048];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] authResponse = packet.getData();
                List<List<String>> authAnswers = DNSrequest.parseAnswers(authResponse);
                List<List<String>> authAuthoritatives = DNSrequest.parseAuthorities(authResponse);
                List<List<String>> authAddiotinals = DNSrequest.parseAdditionals(authResponse);
                /* AUTH SERVER GOT ANSWER */
                if(authAnswers.size() > 0){
                    // RESPONSE
                    try {
                        DNSresponse dnsResponse = new DNSresponse(request, 0, authAnswers, authAuthoritatives, authAddiotinals);
                        rootResponse = dnsResponse.responseMessage();

                        DatagramPacket authPacket = new DatagramPacket(rootResponse, rootResponse.length, this.address, this.port);
                        socket.send(authPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }



                System.out.println("RESPONSE FROM LOCALSERVER");
                DNSresponse dnsResponse = new DNSresponse(request, 0, answers, authoritative, additional);
                byte[] response = dnsResponse.responseMessage();

                packet = new DatagramPacket(response, response.length, this.address, this.port);
                socket.send(packet);
                return;


            }
            catch (IOException e){
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

