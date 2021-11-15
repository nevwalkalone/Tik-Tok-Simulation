package com.example.distrapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.distrapp.phase1Code.AppNode;
import com.example.distrapp.phase1Code.Broker;
import com.example.distrapp.phase1Code.DeepCopy;
import com.example.distrapp.phase1Code.Message;
import com.example.distrapp.phase1Code.Value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class PublishDeleteTask extends AsyncTask<String, String, Void> {

    private Uri uri;


    @SuppressLint("StaticFieldLeak")
    private Context myContext;

    public PublishDeleteTask(){
    }

    public PublishDeleteTask(Context myContext,Uri uri, boolean firstVideo){
        this.uri = uri;
        this.myContext = myContext;


    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected Void doInBackground(String... strings) {
        String video_name = strings[0];
        String taskMessage = strings[1];

        AppNode appNode = new LoggedInUser().getAppNode();

        if (taskMessage.equals("publish")) {

            String hashtags = strings[2];
            //storing any new hashtags
            try {
                ArrayList<String> newHashtags = appNode.getChannel().addVideo(myContext,uri, video_name, hashtags);

                //matching new hashtags to brokers
                for (String newTag:newHashtags){
                    appNode.matchHashes(newTag);
                }

                AppNode new_appNode = appNode.allBytesToNull();
                ArrayList<Value> channel_videos = new_appNode.getChannel().getChannelVideos();
                Value last_video = channel_videos.get(channel_videos.size()-1);

                //notifying each broker for the new video
                //notify each broker for deletion
                ArrayList<Broker> brokers = new_appNode.getBrokers();

                for (Broker broker:brokers){
                    notifyBroker(new_appNode, broker,last_video,"Published a new video!");
                }
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if (taskMessage.equals("delete")){
            ArrayList<Value> channelVideos = appNode.getChannel().getChannelVideos();
            Value video_to_del = null;
            for (Value video:channelVideos){
                if (video_name.equals(video.getVideoName())){
                    video_to_del = video;
                    break;
                }
            }

            ArrayList<String> hashtagsToBeRemoved = appNode.getChannel().removeVideo(video_to_del);

            //removing hashtags from broker match
            for (String hashtag:hashtagsToBeRemoved){
                appNode.getBrokerMatch().remove(hashtag);
            }

            AppNode new_appNode = appNode.allBytesToNull();
            Value empty_video = (Value) DeepCopy.deepCopy(video_to_del);
            empty_video.setVideoFileChunk(null);

            //notify each broker for deletion
            ArrayList<Broker> brokers = new_appNode.getBrokers();
            for (Broker broker:brokers){
                try {
                    notifyBroker(new_appNode,broker, empty_video,"Deleted a video.");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Notify Broker
     * if a video is deleted
     * or published
     **/
    public void notifyBroker(AppNode appNode, Broker broker, Value video, String message) throws IOException {
        Socket appNodeConnection = new Socket(broker.getIP(), broker.getPort());
        ObjectOutputStream out = new ObjectOutputStream(appNodeConnection.getOutputStream());
        ObjectInputStream in =  new ObjectInputStream(appNodeConnection.getInputStream());
        try {

            //sending appropriate message to Broker
            out.writeUTF(message);
            out.flush();

            //sending publisher to broker
            //but with no bytes in his videos,
            //each video has only metadata
            Message m = new Message(appNode);
            out.writeObject(m);
            out.flush();

            //sending video to broker
            Message m2 = new Message(video);
            out.writeObject(m2);
            out.flush();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                appNodeConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
