import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Class to represent a thread for checking new videos
 * in subbed topics and refreshing videos in general
 */
public class RefreshVids extends Thread{

    private ConsAndPubActions consAndPubActions;
    private String channel_name;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // path with all videos
    private String path;

    // Constructor
    public RefreshVids(ConsAndPubActions consAndPubActions, String channel_name, String path){
        this.consAndPubActions = consAndPubActions;
        this.channel_name = channel_name;
        this.path = path;
    }

    /**
     * Run method for the thread to follow
     */
    public void run () {
        try {
            ArrayList<Integer> ports = new ArrayList<>();
            for (Broker brok : consAndPubActions.appNode.getBrokers()){
                ports.add(brok.getPort());
            }
            Collections.sort(ports);

            while(true){

                // sleep for 45 seconds
                Thread.sleep(45000);

                // Random connection with a broker
                double randomNumber = Math.random()*3;
                int rand_number = (int) randomNumber;
                rand_number = 12*rand_number;

                // connect to a random broker to acquire subbed topics hashmap
                // to check for new videos
                Socket consumerConnection = new Socket(consAndPubActions.appNode.getIP(), ports.get(0) + rand_number);
                out = new ObjectOutputStream(consumerConnection.getOutputStream());
                in = new ObjectInputStream(consumerConnection.getInputStream());

                out.writeUTF("Checking for new videos");
                out.flush();

                // reading forUsers hashmap
                Message m = (Message) in.readObject();
                this.consAndPubActions.HashInfo =(HashMap<String, Broker>) m.getData();

                // reading forSubs hashmap
                Message m1 = (Message) in.readObject();
                HashMap<String, ArrayList<Value>> newSubbedTopics =(HashMap<String, ArrayList<Value>>) m1.getData();

                for (String key:this.consAndPubActions.subbedTopics.keySet()){
                    ArrayList<Value> future_videos = newSubbedTopics.get(key);

                    // current state of videos in the specific topic
                    this.consAndPubActions.subbedTopics.replace(key,future_videos);
                    if(future_videos == null){
                        continue;
                    }

                    for (int i = 0; i < future_videos.size(); i++){
                        Value new_video = future_videos.get(i);
                        //System.out.println(new_video.getVideoName());
                        String producer = new_video.getChannelName();

                        // if the same user has published a new video continue
                        if (producer.equals(this.consAndPubActions.appNode.getChannel().getChannelName())){
                            continue;
                        }

                        // we already have this video so continue again
                        if (this.consAndPubActions.savedVideos.contains(new_video.getVideoName())){
                            continue;
                        }

                        // we have already published this video so continue
                        if (this.consAndPubActions.appNode.getChannel().getPublishedVideoNames().contains(new_video.getVideoName())){
                            continue;
                        }

                        // add the video to the saved ones
                        this.consAndPubActions.savedVideos.add(new_video.getVideoName());

                        // finding broker to connect
                        Broker brok_to_connect = this.consAndPubActions.HashInfo.get(key);
                        consumerConnection = new Socket(consAndPubActions.appNode.getIP(), brok_to_connect.getPort());
                        out = new ObjectOutputStream(consumerConnection.getOutputStream());
                        in = new ObjectInputStream(consumerConnection.getInputStream());

                        out.writeUTF("Asking for a new video.");
                        out.flush();

                        // sending topic
                        out.writeUTF(key);
                        out.flush();

                        // sending producer name
                        out.writeUTF(new_video.getChannelName());
                        out.flush();

                        // sending video name
                        out.writeUTF(new_video.getVideoName());
                        out.flush();

                        // saving the mp4 video locally
                        OutputStream chunkToFile = new FileOutputStream(path +"/"+this.consAndPubActions.appNode.getChannel().getChannelName()+"-cons/"+
                                new_video.getVideoName());

                        // reading the number of chunks
                        Message totalChunks = (Message) in.readObject();

                        int numberOfChunks = (int) totalChunks.getData();

                        for (int j=0; j<numberOfChunks; j++){
                            Message temp = (Message) in.readObject();
                            byte[] chunk = (byte[]) temp.getData();
                            chunkToFile.write(chunk);
                        }
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