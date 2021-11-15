package com.example.distrapp.phase1Code;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * CLASS TO REPRESENT
 * A USER, WHO CAN BE A CONSUMER
 * AND A PUBLISHER SIMULTANEOUSLY
 **/

public class AppNode implements Serializable {

    //String,ip,port,
    //channel,brokers etc.
    private String IP;
    private int port;
    private Channel channel;
    public transient AssetManager mngr;
    private ArrayList<Broker> brokers = new ArrayList<>();

    private static final long serialVersionUID = -2723363051271966964L;

    //Integer is the port of the broker to connect
    private HashMap<String,Broker> brokerMatch = new HashMap<>();


    //Constructor
    @RequiresApi(api = Build.VERSION_CODES.P)
    public AppNode(Context myContext, int port, String username, boolean newUser) throws IOException {
        //AppNode port
        this.port = port;

        mngr = myContext.getAssets();

        this.channel = new Channel(mngr);
        this.channel.setChannelName(username);

        //setting IP and port
        //for Brokers
        this.setBrokers();

        //Initializing basic info with brokers
        this.initChannel(newUser, myContext);

    }

    /**
     * Reading and writing to config files to set up Broker
     **/
    private void setBrokers() throws IOException {

        //opening config file
        InputStream is = mngr.open("brokers_for_appnodes.txt");

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String text = new String(buffer);
        String[] splitter = text.split("\n");

        String[] st;

        for (String string: splitter){
            st = string.split(",");

            //add broker to the lis
            brokers.add(new Broker(st[0], Integer.parseInt(st[1].trim())));
        }
    }

    /**
     * Channel Initialization
     **/
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void initChannel(boolean newUser, Context myContext) throws IOException {
            if (newUser == false) {
                this.channel.setUpChannel(myContext);
            }
    }

    /**
     * Calculate hash using sha1 function
     **/
    public int calculateHash (String key){
        String hash = new String(Hex.encodeHex(DigestUtils.sha(key)));
        return new BigInteger(hash, 16).mod(new BigInteger("100")).intValue();
    }

    /**
     * Used for printing
     **/
    public String toString () {

        return "AppNode with IP: " + this.getIP() + " and port: " + this.getPort();
    }


    //-----------------------------------------

    /**
     * Making a deep copy of appnode
     * Also setting all video File chunk values to null
     * to avoid sending whole video in initial connection with brokers,
     * before they make a pull request
     **/
    public AppNode allBytesToNull(){
        AppNode temp = (AppNode) DeepCopy.deepCopy(this);

        Channel temp_channel = temp.getChannel();
        ArrayList<Value> allVideos = temp_channel.getChannelVideos();
        for (Value video: allVideos){
            video.setVideoFileChunk(null);
        }
        return temp;
    }

    /**
     * Update brokerMatch
     **/
    public void matchHashes(String key) {

        int keyHash = this.calculateHash(key);

        if (keyHash >= brokers.get(brokers.size() - 1).getHash()) {
            brokerMatch.put(key, brokers.get(0));
            return;
        }
        for (Broker broker : brokers) {
            if (keyHash <= broker.getHash()) {
                brokerMatch.put(key, broker);
                return;
            }
        }
    }


    //GETTERS
    public String getIP () {
                return IP;
    }

    public int getPort () {

        return port;
    }

    public Channel getChannel () {

        return channel;
    }

    public HashMap<String, Broker> getBrokerMatch () {

        return brokerMatch;
    }

    public ArrayList<Broker> getBrokers () {
        return brokers;
    }


    //-----------------------------------------

    //SETTERS

    public void setChannel (Channel channel) {
        this.channel = channel;
    }

    public void setBrokerMatch () {

    }

    public void setIP (String IP){
        this.IP = IP;
    }

    public void setPort ( int port){
        this.port = port;
    }

}

