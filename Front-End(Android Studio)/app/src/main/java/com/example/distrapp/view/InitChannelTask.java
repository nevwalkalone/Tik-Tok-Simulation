package com.example.distrapp.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.example.distrapp.phase1Code.AppNode;
import com.example.distrapp.phase1Code.Broker;
import com.example.distrapp.phase1Code.Message;
import com.example.distrapp.phase1Code.PubServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class InitChannelTask extends AsyncTask<String, String, Void> {
    AppNode appNode;
    ArrayList<Broker> brokers;

    @SuppressLint("StaticFieldLeak")
    Context myContext;
    int port_int;
    String ip;
    String userName;
    boolean newUser;
    boolean isEmulator;
    Uri uri;

    public InitChannelTask(Context myContext, String ip,int port_int, boolean isEmulator,String userName,  boolean newUser){
        this.myContext = myContext;
        this.port_int = port_int;
        this.ip = ip;
        this.userName = userName;
        this.newUser = newUser;
        this.isEmulator = isEmulator;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected Void doInBackground(String... strings) {
        try {
            String prompt = strings[0];
            LoggedInUser loggedInUser = new LoggedInUser();
            if (prompt.equals("First Initialization")) {
                appNode = new AppNode(myContext, port_int, userName, newUser);
                if (isEmulator) {
                    appNode.setIP("127.0.0.1");
                } else {
                    appNode.setIP(ip);
                }
                loggedInUser.setAppNode(appNode);
                this.brokers = appNode.getBrokers();

                if (newUser == false) {
                    setUpConxToBrokers();
                }
            }
            else if (prompt.equals("firstvideo")) {
                appNode = new LoggedInUser().getAppNode();
                String videoName = strings[1];
                String hashtags = strings[2];
                this.appNode.getChannel().addVideo(myContext, uri, videoName, hashtags);
                this.brokers = appNode.getBrokers();
                setUpConxToBrokers();
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public void setUpConxToBrokers(){
        //connecting to each broker
        //to get hashes
        //etc
        int oldBrokerIndex = 0;
        for (Broker broker : brokers) {
            try {
                initConxToBroker(appNode,broker, oldBrokerIndex, "Init");
            } catch (IOException e) {
                e.printStackTrace();
            }
            oldBrokerIndex++;
        }

        //sorting brokers hashes
        Collections.sort(brokers);

        //Finding and matching brokers
        //for each key
        Set<String> tempKeys = this.appNode.getChannel().getHashtagsPublished();
        tempKeys.add(this.appNode.getChannel().getChannelName());
        for (String key : tempKeys) {
            this.appNode.matchHashes(key);
        }

        //taking a deep copy of
        //appnode with all video bytes
        //equal to null, to avoid
        //sending video, that contains bytes
        AppNode new_app = this.appNode.allBytesToNull();

        //connecting to Broker for
        //2nd time to match hashes
        for (Broker broker : brokers) {
            try {
                initConxToBroker(new_app, broker, oldBrokerIndex, "Matching info");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Publisher Server Thread starting
        Thread pub_thread = new PubServer(this.appNode);
        pub_thread.start();
    }


    /**
     * Connecting with broker
     * to match info
     **/
    public void initConxToBroker(AppNode app, Broker broker, int index, String message) throws IOException {

        Socket appNodeConnection = new Socket(broker.getIP(), broker.getPort());
        ObjectOutputStream out = new ObjectOutputStream(appNodeConnection.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(appNodeConnection.getInputStream());

        try {

            //sending appropriate message to Broker
            out.writeUTF(message);
            out.flush();

            if (message.equals("Init")) {
                //Reading Broker object
                Message temp = (Message) in.readObject();

                //setting broker
                //to the brokers list

                int brokerHash = (int) temp.getData();

                //setting broker hash
                brokers.get(index).setHash(brokerHash);

            }
            else if (message.equals("Matching info")) {

                //sending publisher to broker
                Message m = new Message(app);
                out.writeObject(m);
                out.flush();
            }

        }
        catch (ClassNotFoundException | IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                in.close();
                out.close();
                appNodeConnection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void setUri(Uri uri){
        this.uri = uri;
    }

}
