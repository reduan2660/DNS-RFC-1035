package org.example;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DNSresponse extends MessageFormat{
    public DNSresponse(int _id,  boolean _aa, boolean _recursion, int _rcode, int _qdcount, int _ancount, int _nscount, int _arcount, String _domain, String _qtype) {
        super(_id, true, _aa, _recursion, _rcode, _qdcount, _ancount, _nscount, _arcount, _domain, _qtype);
    }

    static int id(byte[] request){
        /*
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      ID                       |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        int id1 = request[0];
        int id2 = request[1];

        return id2 + id1*512;
    }
    static boolean isRecursive(byte[] request){

        /* RD at bit last bit of third byte

                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      ID                       |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        byte firstFlagByte = request[2];
        int rdBit = firstFlagByte & 1;

        return rdBit == 1;
    }

    static String domain(byte[] request) throws UnsupportedEncodingException {
        /*
        +---------------------+
        |        Header       |   [12 byte]
        +---------------------+
        |       Question      | the question for the name server
        +---------------------+
         */

        /* Qeustion
        +----------------------------------------+
        |       [Label Length - Label -] [] []   |
        +----------------------------------------+

         */

        StringBuilder domain = new StringBuilder();

        int labelLengthIndex = 13 - 1; // 0 indexed
        int labelLength = request[labelLengthIndex];
        labelLengthIndex += 1;

        while (labelLength > 0){

            byte[] labelByte = new byte[labelLength];
            for(int i=0; i<labelLength; i++) labelByte[i] = request[labelLengthIndex+i];

            domain.append(new String(labelByte, StandardCharsets.UTF_8));

            labelLengthIndex += labelLength;
            labelLength = request[labelLengthIndex];

            if(labelLength>0) domain.append(".");
            labelLengthIndex += 1;
        }


        return domain.toString();
    }

    static String domainType(byte[] request){
        StringBuilder domain = new StringBuilder();

        int labelLengthIndex = 13 - 1; // 0 indexed
        int labelLength = request[labelLengthIndex];
        labelLengthIndex += 1;

        while (labelLength > 0){

            byte[] labelByte = new byte[labelLength];
            for(int i=0; i<labelLength; i++) labelByte[i] = request[labelLengthIndex+i];

            domain.append(new String(labelByte, StandardCharsets.UTF_8));

            labelLengthIndex += labelLength;
            labelLength = request[labelLengthIndex];

            if(labelLength>0) domain.append(".");
            labelLengthIndex += 1;
        }


        /* QTYPE */
        /* RFC 1035 QTYPE
        +--+--+--+--+--+--+--+--+--+--+--+--+--+-
        |  TYPE | ID | DESCRIPTION              |
        |    A  | 1  | Address Record           |
        |  AAAA | 28 | IPv6 address record      |
        | CNAME | 5  | Canonical name record    |
        |    MX | 15 | Mail exchange record     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+-
         */

        int qtype = request[labelLengthIndex + 1];

        String _qtype = switch (qtype) {
            case 28 -> "AAAA";
            case 5 -> "CNAME" ;
            case 15 -> "MX" ;
            default -> "A";
        };

        return _qtype;
    }



}
