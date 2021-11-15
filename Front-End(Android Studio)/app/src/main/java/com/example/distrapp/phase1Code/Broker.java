package com.example.distrapp.phase1Code;


import java.math.BigInteger;
import java.io.*;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * CLASS TO REPRESENT A BROKER NODE
 **/

public class Broker implements Comparable , Serializable{

    //IP,port,hash
    private String IP;
    private int port;
    private int hash;

    private static final long serialVersionUID = -2723363051271966964L;

    //List with the publishers

    public Broker(String IP,int port){
        this.IP= IP;
        this.port=port;

    }

    /**
     * Calculate hash using sha1 function
     **/
    public void calculateHash() {
        this.hash = new BigInteger(DigestUtils.sha1Hex(this.getIP() + this.getPort()), 16).mod(new BigInteger("100")).intValue();

    }


    /**
     * Used for printing
     **/
    public String toString(){
        return "Broker with IP: " +this.getIP()+", port: "+this.getPort()+" and hash: " +getHash()+"\n";

    }

    /**
     * Used for sorting
     * an arraylist that contains
     * broker objects
     **/
    @Override
    public int compareTo(Object o) {
        Broker b = (Broker) o;

        return this.getHash()-b.getHash();
    }

    //------------------------------------
    //GETTERS

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;

    }

    public int getHash(){
        return hash;
    }


    //------------------------------------
    //SETTERS

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

}
