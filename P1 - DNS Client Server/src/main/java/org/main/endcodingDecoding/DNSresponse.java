package org.main.endcodingDecoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DNSresponse extends MessageFormat {

    public DNSresponse(byte[] request, int _rcode, List<List<String>> answers, List<List<String>> authoritative, List<List<String>> additional) throws IOException {
        super(parseIdOf(request), true, true, isRecursive(request), _rcode, domain(request), domainType(request), answers, authoritative, additional);
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
    public byte[] responseMessage() throws IOException {

        byte[] request = requestMessage();

        ByteArrayOutputStream responseByteStream = new ByteArrayOutputStream();
        responseByteStream.write(request);
        responseByteStream.write(this.answer);
        responseByteStream.write(this.authority);
        responseByteStream.write(this.additional);
        return responseByteStream.toByteArray();
    }


    /* Extract Information from Request */
    static int parseIdOf(byte[] request){
        /*
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      ID                       |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        int id1 = request[0];
        int id2 = request[1];

        return id2 + id1*256;
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

        /*
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                                               |
        /                     QNAME                     /
        /                                               /
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     QTYPE                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     QCLASS                    |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
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
