package com.example.distrapp.phase1Code;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * CLASS TO REPRESENT
 * A USER, WHO CAN BE A CONSUMER
 * AND A PUBLISHER SIMULTANEOUSLY
 **/

public class AppNode implements Serializable {

    private static final long serialVersionUID = -2723363051271966964L;

    //String,ip,port,
    //channel,brokers etc.
    private String IP;
    private int port;
    private Channel channel;
    private ArrayList<Broker> brokers = new ArrayList<>();

    //Integer is the port of the broker to connect
    private HashMap<String, Broker> brokerMatch = new HashMap<>();


    //Constructor
    public AppNode() {

    }

    /**
     * Calculate hash using sha1 function
     **/
    public int calculateHash(String key) {
        return new BigInteger(DigestUtils.sha1Hex(key), 16).mod(new BigInteger("100")).intValue();
    }

    /**
     * Used for printing
     **/
    public String toString() {

        return "AppNode with IP: " + this.getIP() + " and port: " + this.getPort();
    }


    //-----------------------------------------

    //GETTERS
    public String getIP() {
        return IP;
    }

    public int getPort() {

        return port;
    }
    public Channel getChannel() {

        return channel;
    }

    public HashMap<String, Broker> getBrokerMatch() {

        return brokerMatch;
    }

    public ArrayList<Broker> getBrokers() {
        return brokers;
    }


    //-----------------------------------------

    //SETTERS

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setBrokerMatch(){

    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPort(int port) {
        this.port = port;
    }



}
