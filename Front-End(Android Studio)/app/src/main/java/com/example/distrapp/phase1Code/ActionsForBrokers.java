package com.example.distrapp.phase1Code;

import java.io.*;
import java.net.Socket;
import java.util.*;


/**
 * CLASS TO REPRESENT A THREAD FOR PUSHING VIDEO CHUNKS
 * TO BROKER
 **/

public class ActionsForBrokers extends Thread {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private AppNode appNode;

    //CONSTRUCTOR
    public ActionsForBrokers(Socket connection, AppNode appNode){

        try{
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.appNode = appNode;
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    //Run process to follow
    public void run()  {
        try {
            //reading initial message
            String message = in.readUTF();

            //topic request
            if (message.equals("Searching for key")){
                String key = in.readUTF();
                push_topic(key);
            }
            //video pushing to
            //subscription thread
            else{
                push_new_sub_video();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    //pushing a new specific video to a subscriber
    private void push_new_sub_video() throws IOException {

        //reading video_name
        String video_name = in.readUTF();
        ArrayList<Value> videos = this.appNode.getChannel().getChannelVideos();
        Value video = null;

        //searching for the specific video
        for (Value value:videos){
            if (value.getVideoName().equals(video_name)){
                video = value;
                break;
            }
        }
        ArrayList<byte[]> chunks = this.appNode.getChannel().makeChunks(video.getVideoFileChunk());
        ArrayList<String> hashtags = video.getAssociatedHashtags();

        //sending video hashtags
        Message tags = new Message(hashtags);
        out.writeObject(tags);
        out.flush();

        //sending the total number of chunks
        Message totalChunks = new Message(chunks.size());
        out.writeObject(totalChunks);
        out.flush();

        for (byte[] chunk:chunks){
            Message message = new Message(chunk);
            out.writeObject(message);
            out.flush();
        }
    }


    //pushing videos to a consumer based on a specific topic
    private void push_topic(String key) throws IOException {
        if (!this.appNode.getChannel().getHashtagsPublished().contains(key)){
            out.writeUTF("Key not found");
            out.flush();
            return;
        }
        //everything ok
        out.writeUTF("Key found");
        out.flush();

        ArrayList<Value> videosNeeded;
        HashMap<String, ArrayList<Value>> forLookup;

        if (key.contains("#")){
            forLookup = this.appNode.getChannel().getVideoFiles();
            videosNeeded = forLookup.get(key);
        }
        else{
            videosNeeded = this.appNode.getChannel().getChannelVideos();
        }
        //sending the number of videos
        Message totalVideos = new  Message(videosNeeded.size());
        System.out.println("total videos:"+videosNeeded.size());
        out.writeObject(totalVideos);
        out.flush();

        for (Value video:videosNeeded){
            ArrayList<byte[]> chunks = this.appNode.getChannel().makeChunks(video.getVideoFileChunk());
            ArrayList<String> hashtags = video.getAssociatedHashtags();

            //sending video_name
            out.writeUTF(video.getVideoName());
            out.flush();


            String send_vid_verif = in.readUTF();

            //Consumer has the video, so check next one
            if (send_vid_verif.equals("Don't send it.")){
                continue;
            }

            //sending video hashtags
            Message tags = new Message(hashtags);
            out.writeObject(tags);
            out.flush();


            //sending the total number of chunks
            Message totalChunks = new Message(chunks.size());
            out.writeObject(totalChunks);
            out.flush();

            int counter = 0;
            for (int i=0; i<chunks.size(); i++){
                byte[] chunk = chunks.get(i);
                out.writeUTF("Chunk incoming");
                out.flush();

                Message message = new Message(chunk);
                out.writeObject(message);
                out.flush();
                counter ++;
                }
            }
        }
    }
