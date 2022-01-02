import java.math.BigInteger;
import java.util.*;
import java.io.*;
import java.net.*;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * CLASS TO REPRESENT A BROKER NODE
 **/

public class Broker implements Comparable , Serializable{

    //IP,port,hash
    private String IP;
    private int port;
    private int hash;

    private static final long serialVersionUID = -2723363051271966964L;


    //List with the publishers
    private ArrayList<AppNode> appNodes = new ArrayList<>();

    //hashmap to link each topic with subscribers
    private HashMap<String,ArrayList<AppNode>> subscribers = new HashMap<>();

    //hashmap to be returned to new consumers
    private HashMap<String,Broker> forUsers = new HashMap<>();

    //hashmap to find for each key the associated publishers
    private HashMap<String, ArrayList<AppNode>> keySource = new HashMap<>();

    //hashmap to find for each key the associated videos
    private HashMap<String, ArrayList<Value>> forSubs = new HashMap<>();

    //Socket connection
    private Socket connection = null;


    //com.example.distrapp.phase1Code.Broker Constructor
    public Broker(String IP,int port){
        this.IP= IP;
        this.port=port;

    }

    //Constructor with no arguments
    public Broker () throws IOException {

        //initialize broker
        this.initBroker("config/for_brokers.txt");

        //open com.example.distrapp.phase1Code.Broker Server
        this.openBrokServer();

    }

    /**
     * Reading and writing to config files to set up com.example.distrapp.phase1Code.Broker
     **/
    private void initBroker(String path) throws IOException {

        //File for setting up the
        //specific broker
        File myFile = new File (path);
        FileReader fr =new FileReader(myFile);
        BufferedReader br = new BufferedReader(fr);

        String line =br.readLine();
        String [] st = line.split(" ");

        //setting IP,port,hash
        this.setPort(Integer.parseInt(st[1]));
        this.setIP(st[0]);
        this.calculateHash();

        //writing to the file
        //for the next com.example.distrapp.phase1Code.Broker
        FileWriter myWriter = new FileWriter(path);
        String new_port = Integer.toString(this.port+12);
        myWriter.write(st[0]+" "+new_port);
        myWriter.close();
        fr.close();

    }

    /**
     * Broker Server opening for requests
     **/
    private void openBrokServer() throws IOException {


        ServerSocket brokerSocket = new ServerSocket(port);
        System.out.println("Initialized Broker..");
        System.out.println(this);

        try {
            while(true) {

                // waiting for a connection
                Socket connection = brokerSocket.accept();

                //System.out.println("BROKER GAMW");

                // out: to write to client
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());

                // in: to read from client
                ObjectInputStream in= new ObjectInputStream(connection.getInputStream());

                // reading initial message
                // each message triggers a specific action
                String initMessage = in.readUTF();

                if (initMessage.equals("Init") || initMessage.equals("Matching info") || initMessage.equals("Published a new video!")  || initMessage.equals("Deleted a video.")){

                    // Actions For Publisher Thread
                    Thread pub_thread = new ActionsForPublishers(initMessage,out,in,this);

                    pub_thread.start();
                }
                else{
                    System.out.println("Received Consumer Request.");

                    // Actions for Consumer Thread
                    Thread cons_thread = new ActionsForConsumers(initMessage,out,in,this);
                    cons_thread.start();
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                brokerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Makes/updates hashmaps
     * when a publisher
     * matches his info to brokers
     * for the first time
     **/
    public void makeHashMaps(AppNode newAppnode){


        HashMap<String, ArrayList<Value>> tempForSubs = newAppnode.getChannel().getVideoFiles();
        HashMap<String, ArrayList<Value>> newHashMap = this.getForSubs();
        ArrayList<Value> channelVideos = newAppnode.getChannel().getChannelVideos();

        //if a hashmap is empty
        //that means all hashmaps are
        if (forUsers.isEmpty()){

            //create for users hashmap
            this.forUsers = newAppnode.getBrokerMatch();

            //creating keysource hashmap
            for (String key:newAppnode.getBrokerMatch().keySet()){
                //ArrayList<com.example.distrapp.phase1Code.AppNode> appNodes = new ArrayList<>();
                ArrayList<AppNode> appNodes = new ArrayList<>();
                appNodes.add(newAppnode);
                // appNodes.add(newAppnode);
                //appNodes.add(newAppnode);
                keySource.put(key,appNodes);
            }
            //create for subs hashmap
            this.setForSubs(tempForSubs);
            //put channel name and videos
            this.getForSubs().put(newAppnode.getChannel().getChannelName(),channelVideos);
        }
        else{
            for (String key:newAppnode.getBrokerMatch().keySet()){
                //KeySource HashMap
                if (keySource.containsKey(key)){
                    ArrayList<AppNode> newAppNodes;
                    newAppNodes = keySource.get(key);
                    newAppNodes.add(newAppnode);
                    keySource.replace(key,newAppNodes);

                    ArrayList<Value> newVideos = this.getForSubs().get(key);
                    for (Value v:tempForSubs.get(key)){
                        newVideos.add(v);
                    }
                    newHashMap.replace(key,newVideos);
                }
                else{
                    ArrayList<AppNode> producers = new ArrayList<>();
                    producers.add(newAppnode);

                    keySource.put(key,producers);

                    //add the new key to forUsers Hashmap
                    forUsers.put(key,newAppnode.getBrokerMatch().get(key));

                    //add the new key to forSubs HashMap
                    newHashMap.put(key,tempForSubs.get(key));
                }
            }
            //put com.example.distrapp.phase1Code.Channel name as topic
            newHashMap.put(newAppnode.getChannel().getChannelName(),channelVideos);

        }

        /*for (String string : forUsers.keySet()){
            System.out.println(string +" "+forUsers.get(string));
        }*/
    }

    /**
     * Updates hashmaps
     * after a publisher
     * uploads or deletes a video
     **/
    public void updateHashMaps(AppNode newAppnode, Value video, String message){

        //get video hashtags
        ArrayList<String> hashtags = video.getAssociatedHashtags();
        ArrayList<Value> temp_videos = new ArrayList<>();
        temp_videos.add(video);
        HashMap<String, ArrayList<Value>> newHashMap = this.getForSubs();


        if (message.equals("publish")){

            //refreshing videos for specific channel name
            ArrayList<Value> new_videos = this.getForSubs().get(newAppnode.getChannel().getChannelName());
            new_videos.add(video);
            newHashMap.replace(newAppnode.getChannel().getChannelName(),new_videos);
            for (String hashtag : hashtags){
                if (keySource.containsKey(hashtag)){
                    //updating forSubs hashmap
                    new_videos = this.getForSubs().get(hashtag);
                    new_videos.add(video);
                    newHashMap.replace(hashtag,new_videos);

                    ArrayList<AppNode> newAppNodes;
                    newAppNodes = keySource.get(hashtag);
                    boolean flag = false;
                    for (AppNode app:newAppNodes){
                        if (app.getPort()==newAppnode.getPort()){
                            flag = true;
                            break;
                        }
                    }
                    //this appnode is already
                    //responsible for this topic
                    //no need to add him again
                    if (flag == true){
                        continue;
                    }
                    else{
                        newAppNodes.add(newAppnode);
                        keySource.replace(hashtag,newAppNodes);
                    }
                }
                else{
                    //add the new keys to the hashmaps
                    ArrayList<AppNode> producers = new ArrayList<>();
                    producers.add(newAppnode);
                    keySource.put(hashtag,producers);
                    forUsers.put(hashtag,newAppnode.getBrokerMatch().get(hashtag));

                    //add new key to forSubs hashmap
                    newHashMap.put(hashtag,temp_videos);
                }
            }
        }
        else if (message.equals("delete")){
            ArrayList<Value> producer_vids = this.forSubs.get(video.getChannelName());
            //removing video from the specific producer
            for (Value temp_value : producer_vids){
                if (temp_value.getVideoName().equals(video.getVideoName())){
                    producer_vids.remove(temp_value);
                    this.forSubs.replace(video.getChannelName(), producer_vids);
                    break;
                }
            }
            for (String hashtag:hashtags){
                ArrayList<Value> check_vids =this.getForSubs().get(hashtag);
                if(check_vids.size()==1){
                    this.getForSubs().remove(hashtag);
                    this.getForUsers().remove(hashtag);
                    this.getKeySource().remove(hashtag);
                }
                else{
                    for (Value value: check_vids){
                        if (value.getVideoName().equals(video.getVideoName())){
                            check_vids.remove(value);
                            this.forSubs.replace(hashtag,check_vids);
                            break;
                        }
                        ArrayList<Value> checking = newAppnode.getChannel().getVideoFiles().get(hashtag);
                        if (checking==null){
                            ArrayList<AppNode> apps = this.keySource.get(hashtag);
                            for (AppNode appNode : apps){
                                if (appNode.getPort() == newAppnode.getPort()){
                                    this.keySource.remove(appNode);
                                    break;
                                }
                            }
                        }
                    }
                    this.getForSubs().replace(hashtag,check_vids);
                }
            }
        }
    }

    /**
     * Calculate hash using sha1 function
     **/
    public void calculateHash() {
        this.hash = new BigInteger(DigestUtils.sha1Hex(this.getIP() + this.getPort()), 16).mod(new BigInteger("100")).intValue();

    }

    /**
     * Add appNode to the list of known AppNodes
     **/
    public void addAppnode(AppNode newAppnode){

        this.appNodes.add(newAppnode);
    }

    /**
     * Used for printing
     **/
    public String toString(){
        return "Broker with IP: " +this.getIP()+", port: "+this.getPort()+" and hash: " +getHash()+"\n";

    }

    /**
     * Used for sorting
     * an arraylist that contains
     * broker objects
     **/
    @Override
    public int compareTo(Object o) {
        Broker b = (Broker) o;

        return this.getHash()-b.getHash();
    }

    //------------------------------------
    //GETTERS

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public int getHash() {
        return hash;
    }

    public ArrayList<AppNode> getAppNodes() {
        return appNodes;
    }

    public HashMap<String, ArrayList<AppNode>> getSubscribers() {
        return subscribers;
    }

    public HashMap<String, Broker> getForUsers() {
        return forUsers;
    }

    public HashMap<String, ArrayList<AppNode>> getKeySource() {
        return keySource;
    }

    public HashMap<String, ArrayList<Value>> getForSubs() {
        return forSubs;
    }

    public Socket getConnection() {
        return connection;
    }


    //------------------------------------
    //SETTERS

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public void setAppNodes(ArrayList<AppNode> appNodes) {
        this.appNodes = appNodes;
    }

    public void setSubscribers(HashMap<String, ArrayList<AppNode>> subscribers) {
        this.subscribers = subscribers;
    }

    public void setForUsers(HashMap<String, Broker> forUsers) {
        this.forUsers = forUsers;
    }

    public void setKeySource(HashMap<String, ArrayList<AppNode>> keySource) {
        this.keySource = keySource;
    }

    public void setForSubs(HashMap<String, ArrayList<Value>> forSubs) {
        this.forSubs = forSubs;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }



    //-----------------------------------MAIN---------------------------------
    public static void main(String args[]) throws IOException {
        Broker broker = new Broker();

    }
}
