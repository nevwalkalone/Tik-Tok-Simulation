import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Class to represent a thread for pulling video chunks from publisher
 * and sending them back to consumer
 */
public class ActionsForConsumers extends Thread {

    private ObjectInputStream inFromCons;
    private ObjectOutputStream outToCons;
    private ObjectInputStream inFromPub;
    private ObjectOutputStream outToPub;
    private String message;
    private Broker broker;

    // Constructor
    public ActionsForConsumers(String message, ObjectOutputStream outToCons, ObjectInputStream inFromCons, Broker broker){
        this.message = message;
        this.outToCons = outToCons;
        this.inFromCons = inFromCons;
        this.broker = broker;
    }

    /**
     * Run process for the thread to follow
     */
    public void run(){
        try {
            Message m1 = null;

            switch (this.message) {

                case "Asking for a new video.":
                    pull_new_sub_video();
                    break;

                case "First request.":
                case "Refresh for new videos.":
                    // sending hash map with all needed info
                    m1 = new Message(this.broker.getForUsers());
                    outToCons.writeObject(m1);
                    outToCons.flush();
                    break;

                case "Checking for new videos":
                    // sending hash map with all needed info
                    m1 = new Message(this.broker.getForUsers());
                    outToCons.writeObject(m1);
                    outToCons.flush();
                    // sending hash map with subbed topics
                    m1 = new Message(this.broker.getForSubs());
                    outToCons.writeObject(m1);
                    outToCons.flush();
                    break;

                case "Connected to correct Broker.":
                case "Subscription to topic.":
                    pull_topic();
                    break;
            }
        }
        catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        finally {
            try {
                inFromCons.close();
                outToCons.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    /**
     *  Pulling a specific video
     *  for our subbed topic
     */
    private void pull_new_sub_video() throws IOException, ClassNotFoundException {

        try{
            // reading topic
            String key = inFromCons.readUTF();

            ArrayList<AppNode> appNodes = this.broker.getKeySource().get(key);

            // reading producer
            String producer = inFromCons.readUTF();
            AppNode appNode = null;

            // finding the appNode that published the new video
            for (AppNode app:appNodes){
                if (app.getChannel().getChannelName().equals(producer)){
                    appNode = app;
                    break;
                }
            }
            // reading video_name
            String video_name = inFromCons.readUTF();

            // to Publisher
            Socket consumerConnection = new Socket(appNode.getIP(), appNode.getPort());
            outToPub = new ObjectOutputStream(consumerConnection.getOutputStream());
            inFromPub = new ObjectInputStream(consumerConnection.getInputStream());

            // Sending appropriate message
            outToPub.writeUTF("Searching for new published video.");
            outToPub.flush();

            // sending video_name we want
            outToPub.writeUTF(video_name);
            outToPub.flush();


            // reading the number of chunks
            Message totalChunks = (Message) inFromPub.readObject();

            // sending it to consumer
            outToCons.writeObject(totalChunks);
            outToCons.flush();

            int numberOfChunks = (int) totalChunks.getData();

            for (int j=0; j<numberOfChunks; j++){
                Message temp = (Message) inFromPub.readObject();

                // sending chunk to consumer
                outToCons.writeObject(temp);
                outToCons.flush();

            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                inFromPub.close();
                outToPub.close();
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    /**
     * Pulling videos associated with the specific topic that was requested
     **/
    private void pull_topic() throws IOException, ClassNotFoundException {

        // Reading the topic
        String key = inFromCons.readUTF();

        // reading appNode
        Message newpApp = (Message) inFromCons.readObject();
        AppNode potSub = (AppNode) newpApp.getData();

        int consPort = potSub.getPort();

        if (this.message.equals("Subscription to topic.")) {
            HashMap<String,ArrayList<AppNode>> subs = this.broker.getSubscribers();

            // sending the values that must be sent to consumer that are
            // associated with this specific topic
            ArrayList<Value> values_to_send = this.broker.getForSubs().get(key);
            Message message = new Message(values_to_send);
            outToCons.writeObject(message);
            outToCons.flush();

            if (subs.containsKey(key)) {
                ArrayList<AppNode> newSubs = subs.get(key);
                newSubs.add(potSub);
                subs.replace(key, newSubs);
            } else {
                ArrayList<AppNode> newSubs = new ArrayList<>();
                newSubs.add(potSub);
                subs.put(key, newSubs);
            }
        }

        Socket consumerConnection = null;

        ArrayList<AppNode> appNodes = this.broker.getKeySource().get(key);

        int counter = 0 ;
        if (appNodes == null){

            outToCons.writeUTF("Key not found");
            outToCons.flush();
            return;
        }

        // everything ok
        outToCons.writeUTF("Key found");
        outToCons.flush();

        // for loop to find the number of appnodes
        for (AppNode appNode : appNodes) {
            if (consPort == appNode.getPort()) {
                continue;
            }
            counter++;
        }

        Message appNodeNumber = new Message(counter);

        // sending appnode number containing the specific key
        // to appnode
        outToCons.writeObject(appNodeNumber);
        outToCons.flush();

        try{
            // connecting to appnodes containing the specific topic
            // except for himself
            for (AppNode appNode : appNodes) {

                if (consPort == appNode.getPort()) {
                    continue;
                }

                consumerConnection = new Socket(appNode.getIP(), appNode.getPort());
                outToPub = new ObjectOutputStream(consumerConnection.getOutputStream());
                inFromPub = new ObjectInputStream(consumerConnection.getInputStream());

                outToPub.writeUTF("Searching for key");
                outToPub.flush();

                // sending the key we are searching
                outToPub.writeUTF(key);
                outToPub.flush();

                // sending ok_message to consumer
                String ok_message = inFromPub.readUTF();
                outToCons.writeUTF(ok_message);
                outToCons.flush();

                if (ok_message.equals("Key not found")){
                    continue;
                }
                // reading the number of videos
                Message message = (Message) inFromPub.readObject();


                int totalVideos = (int) message.getData();

                // sending it to consumer
                outToCons.writeObject(message);
                outToCons.flush();

                for (int i = 0; i < totalVideos; i++) {

                    String video_name = inFromPub.readUTF();

                    // sending video_name to consumer
                    outToCons.writeUTF(video_name);
                    outToCons.flush();

                    // verification to send video to consumer
                    String send_vid_verif = inFromCons.readUTF();
                    outToPub.writeUTF(send_vid_verif);
                    outToPub.flush();

                    // Consumer has the video, so check next one
                    if (send_vid_verif.equals("Don't send it.")){
                        continue;
                    }

                    // reading the total number of chunks
                    Message hashtags = (Message) inFromPub.readObject();
                    int totalChunks= (int) hashtags.getData();
                    outToCons.writeObject(hashtags);
                    outToCons.flush();

                    for (int j = 0; j < totalChunks; j++) {
                        Message temp = (Message) inFromPub.readObject();

                        // sending chunk to consumer
                        outToCons.writeObject(temp);
                        outToCons.flush();
                    }
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                if(inFromPub != null && outToPub != null){
                    inFromPub.close();
                    outToPub.close();
                }
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}