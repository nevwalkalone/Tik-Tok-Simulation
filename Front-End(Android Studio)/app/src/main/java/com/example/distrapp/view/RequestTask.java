package com.example.distrapp.view;

//
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.distrapp.phase1Code.AppNode;
import com.example.distrapp.phase1Code.Broker;
import com.example.distrapp.phase1Code.Message;
import com.example.distrapp.phase1Code.RefreshVids;
import com.example.distrapp.phase1Code.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Subscribing to a specific topic
 */
public class RequestTask extends AsyncTask<String,Void,String>{


    public static boolean firstRequest = true;
    private AppNode appNode;
    public static List savedVideos = new ArrayList();

    //topics this consumer is subbed
    public static HashMap<String, ArrayList<Value>> subbedTopics = new HashMap<>();


    public static HashMap<String, Broker> HashInfo = new HashMap<>();
    public static ArrayList<String> allTopics = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    private Context myContext;
    private String topic;
    private ProgressDialog dialog;


    public RequestTask(Context myContext, String topic){
        this.myContext = myContext;
        this.appNode = new LoggedInUser().getAppNode();
        this.topic = topic;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(myContext, "",
                "Streaming Videos. Please wait...", true);
        dialog.show();
    }



    @RequiresApi(api = Build.VERSION_CODES.P)

    public String doInBackground(String... strings){
        boolean flagTemp = false;
        ArrayList<Integer> ports = new ArrayList<>();
        ArrayList<Broker> brokers = appNode.getBrokers();
        for (Broker brok :brokers){
            ports.add(brok.getPort());
        }
        Collections.sort(ports);
        try {
            Socket consumerConnection = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;

            //connect to a random broker
            if (firstRequest) {
                firstRequest = false;
                double randomNumber = Math.random() * 3;
                int rand_number = (int) randomNumber;
                rand_number = 12 * rand_number;
                consumerConnection = new Socket(brokers.get(0).getIP(), ports.get(0) + rand_number);
                out = new ObjectOutputStream(consumerConnection.getOutputStream());
                in = new ObjectInputStream(consumerConnection.getInputStream());

                out.writeUTF("First request.");
                out.flush();

                //reading hashmap from broker
                Message message = (Message) in.readObject();
                HashInfo = (HashMap<String, Broker>) message.getData();

                allTopics.addAll(HashInfo.keySet());

                Thread ref_thread = new RefreshVids(appNode.getChannel().getChannelName(), myContext);
                ref_thread.start();
                return "First Request";
            }

            //can't subscribe to himself
            if (topic.equals(appNode.getChannel().getChannelName())){
                return "You can't subscribe to yourself!";
            }


            Broker newBroker = null;
            for (String key : this.HashInfo.keySet()) {
                if (topic.equals(key)) {
                    newBroker = this.HashInfo.get(key);
                    break;
                }
            }


            if (subbedTopics.containsKey(topic)){
                return "You are already subscribed to this topic!";
            }


            //new connection with correct broker
            consumerConnection = new Socket(newBroker.getIP(), newBroker.getPort());
            out = new ObjectOutputStream(consumerConnection.getOutputStream());
            in = new ObjectInputStream(consumerConnection.getInputStream());

            out.writeUTF("Subscription to topic.");
            out.flush();

            //sending topic to be subscribed
            out.writeUTF(topic);
            out.flush();

            AppNode withNullVideos = this.appNode.allBytesToNull();

            //sending appNode
            Message appNode = new Message(withNullVideos);
            out.writeObject(appNode);
            out.flush();

            //reading values as objects (NO BYTES HAVE BEEN SENT)
            //associated with this topic
            Message new_values = (Message) in.readObject();
            ArrayList<Value> new_videos = (ArrayList<Value>) new_values.getData();


            String ok_message = in.readUTF();
            if (ok_message.equals("Key not found")) {
                return null;
            }

            //reading the number of appnodes
            Message appNodeNumber = (Message) in.readObject();
            int number_of_apps = (int) appNodeNumber.getData();

            subbedTopics.put(topic,new_videos);

            if (number_of_apps == 0) {
                return "No Videos received because you are the only one that has published videos" +
                        "associated with this topic.";
            }
            else{
                for (int k = 0; k < number_of_apps; k++) {
                    //reading verification message
                    ok_message = in.readUTF();
                    if (ok_message.equals("Key not found")) {
                        continue;
                    }
                    //reading the number of videos
                    Message message2 = (Message) in.readObject();
                    int totalVideos = (int) message2.getData();

                    for (int i = 0; i < totalVideos; i++) {

                        //reading video_name
                        String video_name = in.readUTF();

                        //add video_name to savedVideos
                        if (!savedVideos.contains(video_name)) {
                            savedVideos.add(video_name);
                            out.writeUTF("Send it.");
                            out.flush();
                        } else {
                            //Already got the video saved.
                            out.writeUTF("Don't send it.");
                            out.flush();
                            continue;
                        }

                        //reading video hashtags
                        Message hashtags = (Message) in.readObject();
                        ArrayList<String> videoTags= (ArrayList<String>) hashtags.getData();

                        //reading the number of chunks
                        Message message = (Message) in.readObject();
                        int totalChunks = (int) message.getData();


                        //saving the mp4 video locally
                        OutputStream chunkToFile;

                        if ( LoggedInUser.isEmulator()) {
                            File folderFile = new File(myContext.getExternalFilesDir(null).getPath() + File.separator + "Saved");
                            folderFile.mkdir();
                            File myFile = new File(folderFile.getAbsolutePath() + File.separator + video_name);
                            chunkToFile = new FileOutputStream(myFile);
                        } else {
                            File folderFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator + "Saved");
                            folderFile.mkdir();
                            File myFile = new File(folderFile.getAbsolutePath() + File.separator + video_name);
                            chunkToFile = new FileOutputStream(myFile);
                        }

                        for (int j = 0; j < totalChunks; j++) {
                            flagTemp = true;

                            String chunkInc = in.readUTF();

                            if (chunkInc.equals("Chunk incoming")){

                                Message temp = (Message) in.readObject();

                                byte[] chunk = (byte[]) temp.getData();
                                chunkToFile.write(chunk);
                            }
                        }
                        out.writeUTF("All good.");
                        out.flush();


                        //create value object
                        //and add it to array list with consumed videos
                        Value value = new Value();
                        value.setVideoName(video_name);
                        value.setAssociatedHashtags(videoTags);
                        this.appNode.getChannel().extractMetaData(value , myContext, "Saved");
                        this.appNode.getChannel().addConsumedVideos(value);

                        //TODO VGALTO
                        System.out.println("[Consumer] Received video with name: " + video_name + "" +
                                " because you JUST subscribed to topic : " + topic);
                    }
            }
        }
            try {
                in.close();
                out.close();
                consumerConnection.close();
                if (flagTemp){
                  return "Check Your Saved Videos!";
                }
                else{
                    return "Do nothing";
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        catch(IOException | ClassNotFoundException ioException){
            ioException.printStackTrace();
        }
        return null;
    }

    /**
     * Toast messages
     * @param param message to show
     */
    @Override
    protected void onPostExecute(String param){
        dialog.dismiss();
        if(param.equals("Do nothing")){
            Toast.makeText(myContext,
                    "No videos received because you have already saved all videos associated with this topic.",
                    Toast.LENGTH_SHORT).show();
        }
        else if (param.equals("First Request")){
            return;
        }
        else if (param.equals("You are already subscribed to this topic!")){
            Toast.makeText(myContext,
                    "You are already subscribed to this topic!",
                    Toast.LENGTH_SHORT).show();
        }
        else if(param.equals("Check Your Saved Videos!")){
            Toast.makeText(myContext,"Check Your Saved Videos!",
                    Toast.LENGTH_SHORT).show();
        }
        else if (param.equals("No Videos received because you are the only one that has published videos" +
                "associated with this topic.")){
            Toast.makeText(myContext,
                    "No Videos received because you are the only one that has published videos" +
                            "associated with this topic.",
                    Toast.LENGTH_SHORT).show();
        }
        else if(param.equals("You can't subscribe to yourself!")){

            Toast.makeText(myContext,
                    "You can't subscribe to yourself!",
                    Toast.LENGTH_SHORT).show();
        }

    }

}

