package com.example.distrapp.phase1Code;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.example.distrapp.view.LoggedInUser;
import com.example.distrapp.view.RequestTask;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;


/**
 * CLASS TO REPRESENT A THREAD
 * FOR CHECKING NEW VIDEOS
 * IN SUBBED TOPICS
 * AND REFESHING VIDEOS
 * IN GENERAL
 **/

public class RefreshVids extends Thread{


    private String channel_name;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Context myContext;
    //path with all videos

    private AppNode appNode;

    //CONSTRUCTOR
    public RefreshVids( String channel_name, Context myContext){
        this.myContext = myContext;
        this.channel_name = channel_name;
        this.appNode = new LoggedInUser().getAppNode();
    }

    //Run process to follow
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void run () {
        try {
            ArrayList<Integer> ports = new ArrayList<>();
            ArrayList<Broker> brokers = appNode.getBrokers();
            for (Broker brok : brokers){
                ports.add(brok.getPort());
            }
            Collections.sort(ports);

            while(true){

                //sleep for 45 seconds
                Thread.sleep(20000);

                //Random connection with a broker
                double randomNumber = Math.random()*3;
                int rand_number = (int) randomNumber;
                rand_number = 12*rand_number;

                //connect to a random broker to acquire subbed topics hashmap
                //to check for new videos

                Socket consumerConnection = new Socket(brokers.get(0).getIP(), ports.get(0) + rand_number);
                out = new ObjectOutputStream(consumerConnection.getOutputStream());
                in = new ObjectInputStream(consumerConnection.getInputStream());

                out.writeUTF("Checking for new videos");
                out.flush();

                //reading forUsers hashmap
                Message m = (Message) in.readObject();

                //refreshing hashmap
                RequestTask.HashInfo =(HashMap<String, Broker>) m.getData();
                Collection<String> all_topics = RequestTask.HashInfo.keySet();

                RequestTask.allTopics = new ArrayList<>(all_topics);


               // ArrayList<String> tempArr = (ArrayList<String>) RequestTask.HashInfo.keySet();
               // RequestTask.allTopics = tempArr;

                //reading forSubs hashmap
                Message m1 = (Message) in.readObject();
                HashMap<String, ArrayList<Value>> newSubbedTopics =(HashMap<String, ArrayList<Value>>) m1.getData();

                for (String key:RequestTask.subbedTopics.keySet()){
                    ArrayList<Value> future_videos = newSubbedTopics.get(key);

                    //current state of videos in the specific topic
                    RequestTask.subbedTopics.replace(key,future_videos);

                    if(future_videos == null){
                        continue;
                    }

                    for (int i = 0; i < future_videos.size(); i++) {


                        Value new_video = future_videos.get(i);
                        //System.out.println(new_video.getVideoName());
                        String producer = new_video.getChannelName();

                        //if the same user published a new video
                        //continue
                        if (producer.equals(channel_name)) {
                            continue;
                        }
                        //we already have this video
                        if (RequestTask.savedVideos.contains(new_video.getVideoName())) {
                            continue;
                        }

                        //add the video to the saved ones
                        RequestTask.savedVideos.add(new_video.getVideoName());

                        //finding broker to connect
                        Broker brok_to_connect = RequestTask.HashInfo.get(key);
                        consumerConnection = new Socket(brok_to_connect.getIP(), brok_to_connect.getPort());
                        out = new ObjectOutputStream(consumerConnection.getOutputStream());
                        in = new ObjectInputStream(consumerConnection.getInputStream());

                        out.writeUTF("Asking for a new video.");
                        out.flush();

                        //sending topic
                        out.writeUTF(key);
                        out.flush();

                        //sending producer name
                        out.writeUTF(new_video.getChannelName());
                        out.flush();

                        //sending video name
                        out.writeUTF(new_video.getVideoName());
                        out.flush();

                        //saving the mp4 video locally
                        OutputStream chunkToFile;

                        if (LoggedInUser.isEmulator()) {
                            File folderFile = new File(myContext.getExternalFilesDir(null).getPath() + File.separator + "Saved");
                            folderFile.mkdir();
                            File myFile = new File(folderFile.getAbsolutePath() + File.separator + new_video.getVideoName());
                            chunkToFile = new FileOutputStream(myFile);
                        } else {
                            File folderFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator + "Saved");
                            folderFile.mkdir();
                            File myFile = new File(folderFile.getAbsolutePath() + File.separator + new_video.getVideoName());
                            chunkToFile = new FileOutputStream(myFile);
                        }

                        //reading video hashtags
                        Message hashtags = (Message) in.readObject();
                        ArrayList<String> videoTags= (ArrayList<String>) hashtags.getData();

                        //reading the number of chunks
                        Message totalChunks = (Message) in.readObject();

                        int numberOfChunks = (int) totalChunks.getData();

                        for (int j = 0; j < numberOfChunks; j++) {
                            Message temp = (Message) in.readObject();
                            byte[] chunk = (byte[]) temp.getData();
                            chunkToFile.write(chunk);
                        }

                        out.writeUTF("All good.");
                        out.flush();

                        //create value object
                        //and add it to array list with consumed videos
                        Value value = new Value();
                        value.setVideoName(new_video.getVideoName());
                        value.setAssociatedHashtags(videoTags);
                        this.appNode.getChannel().extractMetaData(value , myContext, "Saved");
                        this.appNode.getChannel().addConsumedVideos(value);
                        new LoggedInUser().setNewVideos(true);

                        //TODO VGALTO
                        System.out.println("[Acting as a Consumer] Received new video with name: " +new_video.getVideoName()+"" + " because you are subscribed to topic: "+key);
                    }
                }
            }
        }
        catch (IOException | ClassNotFoundException | InterruptedException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}