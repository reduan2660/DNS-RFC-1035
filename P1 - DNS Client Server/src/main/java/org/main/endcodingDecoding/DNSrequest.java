package org.main.endcodingDecoding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DNSrequest extends  MessageFormat {
    public DNSrequest(int _id, boolean _recursion, String _domain, String _qtype) {
        super(_id, false, false, _recursion, 0, 1, 0, 0, 0, _domain, _qtype);
    }

    /* Combine Message Information and Generate Byte Messages to send */
    public byte[] requestMessage(){
        byte[] header = new byte[12];

        header[0] = this.id[0]; header[1] = this.id[1];
        header[2] = this.flag[0]; header[3] = this.flag[1];
        header[4] = this.qdcount[0]; header[5] = this.qdcount[1];
        header[6] = this.ancount[0]; header[7] = this.ancount[1];
        header[8] = this.nscount[0]; header[9] = this.nscount[1];
        header[10] = this.arcount[0]; header[11] = this.arcount[1];


        byte[] question = new byte[this.qname.length + 2 + 2];
        int i=0;
        for(i=0; i<this.qname.length; i++) question[i] = this.qname[i];

        question[i] = this.qtype[0]; i++; question[i] = this.qtype[1]; i++;
        question[i] = this.qclass[0]; i++; question[i] = this.qclass[1]; i++;

        byte[] request = new byte[header.length + question.length];
        i=0;
        for(i=0; i<header.length; i++) request[i] = header[i];
        for(; i<header.length + question.length; i++) request[i] = question[i - header.length];

        return request;
    }

    /* Extract Information from response */
    public static int parseIdOf(byte[] response){
        /*
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      ID                       |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        int id1 = response[0];
        int id2 = response[1];

        return id2 + id1*256;
    }

    public static int parseANCOUNT(byte[] response){
        /* HEADER
                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

    */
        int ancount1 = response[6];
        int ancount2 = response[7];

        return ancount2 + ancount1*512;
    }

    public static int parseNSCOUNT(byte[] response){
        /* HEADER
                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

    */
        int nscount1 = response[8];
        int nscount2 = response[9];

        return nscount2 + nscount1*512;
    }

    public static int parseARCOUNT(byte[] response){
        /* HEADER
                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

    */
        int arcount1 = response[10];
        int arcount2 = response[11];

        return arcount2 + arcount1*512;
    }

    public static List<List<String>> parseAnswers(byte[] response){
        int index = 0;
        index += 12; // 12 BIT HEADER

        // OFFSETTING For Question

        // QNAME
        int labelLength = response[index];
        while(labelLength > 0){
            index += labelLength + 1;
            labelLength = response[index];
        }
        index += 1;

        // QTYPE & QCLASS
        index += 4;


        /* ANSWER Territory
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                                               |
        /                                               /
        /                      NAME                     /
        |                                               |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      TYPE                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     CLASS                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      TTL                      |
        |                                               |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                   RDLENGTH                    |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
        /                     RDATA                     /
        /                                               /
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

         */
        List<List<String>> answers = new ArrayList<>();
        for(int ancounti =0; ancounti < parseANCOUNT(response); ancounti++) {

            // NAME
            StringBuilder domain = new StringBuilder();

            labelLength = response[index];

            while (labelLength > 0) {
                index += 1;

                byte[] labelByte = new byte[labelLength];
                for (int i = 0; i < labelLength; i++) labelByte[i] = response[index + i];

                domain.append(new String(labelByte, StandardCharsets.UTF_8));

                index += labelLength;
                labelLength = response[index];

                if (labelLength > 0) domain.append(".");
            }

            // TYPE
            int _type = response[index + 2];
            String atype = switch (_type) {
                case 28 -> "AAAA";
                case 5 -> "CNAME";
                case 15 -> "MX";
                case 1 -> "A";
                default -> "ERROR";
            };

            // CLASS
            index += 5; // TYPE + CLASS
            long ttl1 = response[index] & 0xffL;  // UNSIGNED
            long ttl2 = response[index + 1] & 0xffL;  // UNSIGNED
            long ttl = ttl2 + ttl1 * 256;

            // RDLENGTH
            index += 2;
            long rdlength1 = response[index] & 0xffL;  // UNSIGNED
            long rdlength2 = response[index + 1] & 0xffL;  // UNSIGNED
            int rdlength = (int) ((int) rdlength2 + rdlength1 * 256);

            // RDATA
            index += 2;
            StringBuilder rdata = new StringBuilder();
            byte[] rdataByte = new byte[rdlength];
            for (int i = 0; i < rdlength; i++) {
                rdataByte[i] = response[index + i];
            }
            rdata.append(new String(rdataByte, StandardCharsets.UTF_8));

            List<String> answer = new ArrayList<>();
            answer.add(rdata.toString());
            answer.add(atype);
            answer.add(String.valueOf(ttl));
            answers.add(answer);
        }
        return answers;
    }

    public static List<List<String>> parseAuthorities(byte[] response){
        int index = 0;
        index += 12; // 12 BIT HEADER

        // OFFSETTING For Question

        // QNAME
        int labelLength = response[index];
        while(labelLength > 0){
            index += labelLength + 1;
            labelLength = response[index];
        }
        index += 1;

        // QTYPE & QCLASS
        index += 4;

        // ANSWERS
        for(int ancounti =0; ancounti < parseANCOUNT(response); ancounti++) {

            // NAME
            labelLength = response[index];

            while (labelLength > 0) {
                index += 1;
                index += labelLength;
                labelLength = response[index];
            }

            index += 5; // TYPE + CLASS

            // RDLENGTH
            index += 2;
            long rdlength1 = response[index] & 0xffL;  // UNSIGNED
            long rdlength2 = response[index + 1] & 0xffL;  // UNSIGNED
            int rdlength = (int) ((int) rdlength2 + rdlength1 * 256);

            // RDATA
            index += 2;
            index += rdlength;
        }

        /* AUTHORITIES Territory
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                                               |
        /                                               /
        /                      NAME                     /
        |                                               |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      TYPE                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     CLASS                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      TTL                      |
        |                                               |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                   RDLENGTH                    |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
        /                     RDATA                     /
        /                                               /
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

         */
        List<List<String>> authorities = new ArrayList<>();
        for(int nscounti =0; nscounti < parseNSCOUNT(response); nscounti++) {

            // NAME
            StringBuilder domain = new StringBuilder();

            labelLength = response[index];

            while (labelLength > 0) {
                index += 1;

                byte[] labelByte = new byte[labelLength];
                for (int i = 0; i < labelLength; i++) labelByte[i] = response[index + i];

                domain.append(new String(labelByte, StandardCharsets.UTF_8));

                index += labelLength;
                labelLength = response[index];

                if (labelLength > 0) domain.append(".");
            }

            // TYPE
            int _type = response[index + 2];
            String atype = switch (_type) {
                case 2 -> "NS";
                case 28 -> "AAAA";
                case 5 -> "CNAME";
                case 15 -> "MX";
                case 1 -> "A";
                default -> "ERROR";
            };

            // CLASS
            index += 5; // TYPE + CLASS
            long ttl1 = response[index] & 0xffL;  // UNSIGNED
            long ttl2 = response[index + 1] & 0xffL;  // UNSIGNED
            long ttl = ttl2 + ttl1 * 256;

            // RDLENGTH
            index += 2;
            long rdlength1 = response[index] & 0xffL;  // UNSIGNED
            long rdlength2 = response[index + 1] & 0xffL;  // UNSIGNED
            int rdlength = (int) ((int) rdlength2 + rdlength1 * 256);

            // RDATA
            index += 2;
            StringBuilder rdata = new StringBuilder();
            byte[] rdataByte = new byte[rdlength];
            for (int i = 0; i < rdlength; i++) {
                rdataByte[i] = response[index + i];
            }
            rdata.append(new String(rdataByte, StandardCharsets.UTF_8));
            index += rdlength;

            List<String> authority = new ArrayList<>();
            authority.add(rdata.toString());
            authority.add(atype);
            authority.add(String.valueOf(ttl));
            authorities.add(authority);
        }
        return authorities;
    }

    public static List<List<String>> parseAdditionals(byte[] response){
        int index = 0;
        index += 12; // 12 BIT HEADER

        // OFFSETTING For Question

        // QNAME
        int labelLength = response[index];
        while(labelLength > 0){
            index += labelLength + 1;
            labelLength = response[index];
        }
        index += 1;

        // QTYPE & QCLASS
        index += 4;

        // OFFSETTING For Answers
        // ANSWERS
        for(int ancounti =0; ancounti < parseANCOUNT(response); ancounti++) {

            // NAME
            labelLength = response[index];

            while (labelLength > 0) {
                index += 1;
                index += labelLength;
                labelLength = response[index];
            }

            index += 5; // TYPE + CLASS

            // RDLENGTH
            index += 2;
            long rdlength1 = response[index] & 0xffL;  // UNSIGNED
            long rdlength2 = response[index + 1] & 0xffL;  // UNSIGNED
            int rdlength = (int) ((int) rdlength2 + rdlength1 * 256);

            // RDATA
            index += 2;
            index += rdlength;
        }


        // Authoritatives
        // ANSWERS
        for(int nscounti =0; nscounti < parseNSCOUNT(response); nscounti++) {

            // NAME
            labelLength = response[index];

            while (labelLength > 0) {
                index += 1;
                index += labelLength;
                labelLength = response[index];
            }

            index += 5; // TYPE + CLASS

            // RDLENGTH
            index += 2;
            long rdlength1 = response[index] & 0xffL;  // UNSIGNED
            long rdlength2 = response[index + 1] & 0xffL;  // UNSIGNED
            int rdlength = (int) ((int) rdlength2 + rdlength1 * 256);

            // RDATA
            index += 2;
            index += rdlength;
        }



        /* ADDITIONALS Territory
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                                               |
        /                                               /
        /                      NAME                     /
        |                                               |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      TYPE                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     CLASS                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      TTL                      |
        |                                               |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                   RDLENGTH                    |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
        /                     RDATA                     /
        /                                               /
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

         */
        List<List<String>> additionals = new ArrayList<>();
        for(int arcounti =0; arcounti < parseARCOUNT(response); arcounti++) {

            // NAME
            StringBuilder domain = new StringBuilder();

            labelLength = response[index];

            while (labelLength > 0) {
                index += 1;

                byte[] labelByte = new byte[labelLength];
                for (int i = 0; i < labelLength; i++) labelByte[i] = response[index + i];

                domain.append(new String(labelByte, StandardCharsets.UTF_8));

                index += labelLength;
                labelLength = response[index];

                if (labelLength > 0) domain.append(".");
            }

            // TYPE
            int _type = response[index + 2];
            String atype = switch (_type) {
                case 2 -> "NS";
                case 28 -> "AAAA";
                case 5 -> "CNAME";
                case 15 -> "MX";
                case 1 -> "A";
                default -> "ERROR";
            };

            // CLASS
            index += 5; // TYPE + CLASS
            long ttl1 = response[index] & 0xffL;  // UNSIGNED
            long ttl2 = response[index + 1] & 0xffL;  // UNSIGNED
            long ttl = ttl2 + ttl1 * 256;

            // RDLENGTH
            index += 2;
            long rdlength1 = response[index] & 0xffL;  // UNSIGNED
            long rdlength2 = response[index + 1] & 0xffL;  // UNSIGNED
            int rdlength = (int) ((int) rdlength2 + rdlength1 * 256);

            // RDATA
            index += 2;
            StringBuilder rdata = new StringBuilder();
            byte[] rdataByte = new byte[rdlength];
            for (int i = 0; i < rdlength; i++) {
                rdataByte[i] = response[index + i];
            }
            rdata.append(new String(rdataByte, StandardCharsets.UTF_8));
            index += rdlength;

            List<String> additional = new ArrayList<>();
            additional.add(rdata.toString());
            additional.add(atype);
            additional.add(String.valueOf(ttl));
            additionals.add(additional);
        }
        return additionals;
    }


}


