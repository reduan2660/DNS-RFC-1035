package org.example;

/* RFC 1035 */

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/*
+---------------------+
|        Header       |
+---------------------+
|       Question      | the question for the name server
+---------------------+
|        Answer       | RRs answering the question
+---------------------+
|      Authority      | RRs pointing toward an authority
+---------------------+
|      Additional     | RRs holding additional information
+---------------------+

*/
public class MessageFormat {

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

    byte[] id = new byte[2]; // A 16 bit identifier
    byte[] flag = new byte[2]; // A 16 bit flag
    byte[] qdcount = new byte[2]; // A 16 bit qdcount
    byte[] ancount = new byte[2]; // A 16 bit ancount
    byte[] nscount = new byte[2]; // A 16 bit nscount
    byte[] arcount = new byte[2]; // A 16 bit arcount



    /* Question section format
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

    byte[] qname;
    byte[] qtype = new byte[2]; // A 16 bit qtype
    byte[] qclass = new byte[2]; // A 16 bit qclass

    public MessageFormat(
            int _id, boolean _response, boolean _aa, boolean _recursion, int _rcode, int _qdcount, int _ancount, int _nscount, int _arcount,
            String domain, String qtype
    ){

        /* Converting integer id to 2 byte array */
        this.id[1] = (byte) (_id & 0xFF);
        this.id[0] = (byte) ((_id >> 8) & 0xFF);

        /* FLAG
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        // query (0), or a response (1).
        if(_response) this.flag[0]  |=  (1 << 7);
        else this.flag[0]  &=  ~(1 << 7);

        //  A four bit field; 0 => a standard query (QUERY) // -0000---
        this.flag[0]  &=  ~(1 << 6); this.flag[0]  &=  ~(1 << 5); this.flag[0]  &=  ~(1 << 4); this.flag[0]  &=  ~(1 << 3);

        // Authoritative Answer - this bit is valid in responses
        if(_aa) this.flag[0]  |=  (1 << 2); // -----1--
        else this.flag[0]  &=  ~(1 << 2); // -----0--

        // TrunCation - specifies that this message was truncated; default => 0
        this.flag[0]  &=  ~(1 << 1); // ------0-

        // Recursion Desired
        if(_recursion) this.flag[0]  |=  (1 << 0); // -------1
        else this.flag[0]  &=  ~(1 << 0); // -------0

        // Recursion Available
        if(_recursion) this.flag[1]  |=  (1 << 7); // 1-------
        else this.flag[1]  &=  ~(1 << 7); // 0-------

        // Reserved | Must be zero
        this.flag[1]  &=  ~(1 << 6); this.flag[1]  &=  ~(1 << 5); this.flag[1]  &=  ~(1 << 4); // -000----

        // Response code - 4 bit field; 0 => No error condition, 1 => Format error, 2=>Server failure, 3=>Name error, 4=>Not implemented, 5=>Refused
        this.flag[1]  &=  ~(1 << 3); this.flag[1]  &=  ~(1 << 2); this.flag[1]  &=  ~(1 << 1); // ----000-
        if(_rcode == 1) this.flag[1]  |=  (1 << 0);  // -------1
        else this.flag[1]  &=  ~(1 << 0); // -------0


        /* QDCOUNT */
        /* Converting integer id to 2 byte array */
        this.qdcount[1] = (byte) (_qdcount & 0xFF);
        this.qdcount[0] = (byte) ((_qdcount >> 8) & 0xFF);

        /* ANCOUNT */
        /* Converting integer id to 2 byte array */
        this.ancount[1] = (byte) (_ancount & 0xFF);
        this.ancount[0] = (byte) ((_ancount >> 8) & 0xFF);

        /* NSCOUNT */
        /* Converting integer id to 2 byte array */
        this.nscount[1] = (byte) (_nscount & 0xFF);
        this.nscount[0] = (byte) ((_nscount >> 8) & 0xFF);

        /* ARCOUNT */
        /* Converting integer id to 2 byte array */
        this.arcount[1] = (byte) (_arcount & 0xFF);
        this.arcount[0] = (byte) ((_arcount >> 8) & 0xFF);



        /* Question section format
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

        /* QNAME */
        /* [ Label Length - LABEL - ] */

        String[] labels = domain.split("\\.");
        byte[] _qname = new byte[labels.length + domain.length()];

        int i = 0;

        // Copying label length and label to qname one-by-one
        for(int j=0; j<labels.length; j++){
            _qname[i] = (byte) labels[j].length();
            i += 1;

            byte[] labelByte = labels[j].getBytes();
            for(int k=0; k<labelByte.length; k++) _qname[i+k] = labelByte[k];

            i += labelByte.length;
        }

        this.qname = _qname;





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

        int _qtype = switch (qtype) {
            case "AAAA" -> 28;
            case "CNAME" -> 5;
            case "MX" -> 15;
            default -> 1;
        };

        /* Converting integer id to 2 byte array */
        this.qtype[1] = (byte) (_qtype & 0xFF);
        this.qtype[0] = (byte) ((_qtype >> 8) & 0xFF);


        /* QCLASS */
        /* DEFAULT 1 for INTERNET */
        /* Converting integer id to 2 byte array */
        this.qclass[1] = (byte) (1 & 0xFF);
        this.qclass[0] = (byte) ((1 >> 8) & 0xFF);

    }
    public void printMessage(){
        System.out.print(String.format("%8s", Integer.toBinaryString((this.id[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.id[1] + 256) % 256)).replace(' ', '0'));

        System.out.print(String.format("%8s", Integer.toBinaryString((this.flag[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.flag[1] + 256) % 256)).replace(' ', '0'));

        System.out.print(String.format("%8s", Integer.toBinaryString((this.qdcount[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.qdcount[1] + 256) % 256)).replace(' ', '0'));

        System.out.print(String.format("%8s", Integer.toBinaryString((this.ancount[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.ancount[1] + 256) % 256)).replace(' ', '0'));

        System.out.print(String.format("%8s", Integer.toBinaryString((this.nscount[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.nscount[1] + 256) % 256)).replace(' ', '0'));

        System.out.print(String.format("%8s", Integer.toBinaryString((this.arcount[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.arcount[1] + 256) % 256)).replace(' ', '0'));


        for(int i=0; i<this.qname.length; i++) System.out.print(String.format("%8s", Integer.toBinaryString((this.qname[i] + 256) % 256)).replace(' ', '0') + " ");
        System.out.println();

        System.out.print(String.format("%8s", Integer.toBinaryString((this.qtype[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.qtype[1] + 256) % 256)).replace(' ', '0'));

        System.out.print(String.format("%8s", Integer.toBinaryString((this.qclass[0] + 256) % 256)).replace(' ', '0'));
        System.out.println(String.format("%8s", Integer.toBinaryString((this.qclass[1] + 256) % 256)).replace(' ', '0'));


    }
}

