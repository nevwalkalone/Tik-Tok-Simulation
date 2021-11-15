package com.example.distrapp.phase1Code;

import java.io.*;
import java.util.*;



/**
 * CLASS TO REPRESENT THE CHANNEL OF A PRODUCER
 **/

public class Channel implements Serializable {

    public String channelName;

    private Set<String> hashtagsPublished = new HashSet<>();

    private ArrayList<Value> channelVideos = new ArrayList<>();

    //Hashmap storing videos linked with a specific topic
    private HashMap<String, ArrayList<Value>> userVideoFilesMap = new HashMap<>();

    private static final long serialVersionUID = -2723363051271966964L;

    //Constructors
    public Channel(){

    }

    public Channel(String channelName){

        this.channelName = channelName;
    }

    //-----------------------------------------

    //GETTERS

    public Set<String> getHashtagsPublished() {

        return hashtagsPublished;
    }

    public ArrayList<Value> getChannelVideos() {

        return channelVideos;
    }

    public String getChannelName() {

        return channelName;
    }

    public HashMap<String, ArrayList<Value>> getVideoFiles() {

        return userVideoFilesMap;
    }


}
