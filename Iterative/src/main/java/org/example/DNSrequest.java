package org.example;

public class DNSrequest extends  MessageFormat {
    public DNSrequest(int _id, boolean _recursion, String _domain, String _qtype) {
        super(_id, false, false, _recursion, 0, 1, 0, 0, 0, _domain, _qtype);
    }

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
}

