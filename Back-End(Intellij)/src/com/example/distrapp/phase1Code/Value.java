package com.example.distrapp.phase1Code;

import java.io.Serializable;
import java.util.ArrayList;


/*
 * CLASS TO REPRESENT A VIDEO OBJECT
 */

public class Value implements Serializable {

    private String videoName;
    private String channelName;
    private String dateCreated;
    private long length;
    private long frameRate;
    private String frameWidth;
    private String frameHeight;
    private ArrayList<String> associatedHashtags;
    private byte[] videoFileChunk;

    private static final long serialVersionUID = -2723363051271966964L;
    //Default Constructor
    public Value(){

    }

    //for printing purposes
    public String toString(){
        return " Videoname: " +this.getVideoName()+
                ", found in com.example.distrapp.phase1Code.Channel: "+
                this.getChannelName()+", date created: "+this.getDateCreated()+"\n"+this.getFrameWidth()
                +"\n"+this.getFrameHeight()+"\n"+this.getLength()+"\n"+this.getAssociatedHashtags();
    }


    //GETTERS
    public String getVideoName() {
        return videoName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public long getLength() {
        return length;
    }

    public long getFramerate() {
        return frameRate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public ArrayList<String> getAssociatedHashtags() {
        return associatedHashtags;
    }

    public byte[] getVideoFileChunk() {
        return videoFileChunk;
    }


    //SETTERS
    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setFramerate(long frameRate) {
        this.frameRate = frameRate;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    public void setAssociatedHashtags(ArrayList<String> associatedHashtags) {
        this.associatedHashtags = associatedHashtags;
    }
    public void setVideoFileChunk(byte[] videoFileChunk) {
        this.videoFileChunk = videoFileChunk;
    }
}
