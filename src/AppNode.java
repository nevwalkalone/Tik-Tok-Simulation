import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Class to represent a user, who can be a consumer
 * and a publisher simultaneously
 */
public class AppNode implements Serializable {

    private static final long serialVersionUID = -2723363051271966964L;

    // String, ip, port, channel, brokers, etc.
    private String IP;
    private int port;
    private Channel channel;
    private ArrayList<Broker> brokers = new ArrayList<>();

    // Integer is the port of the broker to connect
    private HashMap<String, Broker> brokerMatch = new HashMap<>();


    // Constructor
    public AppNode() throws IOException, TikaException, SAXException {

        // setting IP and port for AppNode
        this.readFile("config/for_AppNodes.txt");

        // setting IP and port for each broker
        this.setBrokers("config/brokers_for_appnodes.txt");

        // Menu to choose
        this.printMenu();
    }

    /**
     * Reading and writing to config files to set up Broker
     * @param path
     * @throws IOException
     */
    private void readFile(String path) throws IOException {
        File myFile = new File(path);
        FileReader fr = new FileReader(myFile);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        String[] st = line.split(" ");

        //setting IP,port
        this.setPort(Integer.parseInt(st[1]));
        this.setIP(st[0]);

        //writing to the file for the next AppNode
        FileWriter myWriter = new FileWriter(path);
        String new_port = Integer.toString(this.port + 1);
        myWriter.write(st[0] + " " + new_port);
        myWriter.close();
        fr.close();
    }

    /**
     * Menu choices
     * @throws IOException
     */
    public void printMenu() throws IOException {

        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        File temp_file = new File( System.getProperty("user.dir")+"//temp.txt");
        String txtfile =  temp_file.getParentFile().getParent()+"/videos_dataset";

        System.out.println("Username:");
        String prodName = reader.readLine();

        this.channel = new Channel();

        this.channel.channelName = prodName;

        System.out.println("Initialize your channel with personal videos?");
        System.out.println("1.Yes (/your_username directory must exist with initialized videos)");
        System.out.println("2.No");

        Scanner scan = new Scanner(System.in);
        int choice = scan.nextInt();

        // Starting as a consumer only
        if (choice == 2) {

            // Consumer Thread starting
            Thread cons_thread = new ConsAndPubActions(this,false,txtfile);
            cons_thread.start();
        }
        // Initializing channel
        else {
            init(false);
        }
    }

    /**
     * Channel Initialization
     * @param init_after boolean value that tells us if the channel is being
     *                   initialized after the user has first become a consumer
     */
    public void init(boolean init_after) {
        try {
            File temp_file = new File( System.getProperty("user.dir")+"//temp.txt");
            String txtfile =  temp_file.getParentFile().getParent()+"/videos_dataset";
            if(init_after == false){
                this.channel.setUpChannel(txtfile);
            }
            // First video to be added to the channel
            else{
                this.channel.addVideo(txtfile,true);
            }
            System.out.println("Videos Initialization was successful!\n");

            // connecting to each broker to get hashes
            int oldBrokerIndex = 0;
            for (Broker broker : brokers) {
                initConxToBroker(this,broker, oldBrokerIndex, "Init");
                oldBrokerIndex++;
            }

            // sorting brokers hashes
            Collections.sort(brokers);

            // Finding and matching brokers for each key
            Set<String> tempKeys = this.channel.getHashtagsPublished();
            tempKeys.add(this.channel.getChannelName());
            for (String key : tempKeys) {
                this.matchHashes(key);
            }

            // taking a deep copy of AppNode with all video bytes
            // equal to null, to avoid sending videos that contain bytes
            AppNode new_app = allBytesToNull();

            // connecting to Brokers for the 2nd time to match hashes
            for (Broker broker : brokers) {
                initConxToBroker(new_app, broker, oldBrokerIndex, "Matching info");
            }
            // Publisher Server Thread starting
            Thread pub_thread = new PubServer(this, init_after);
            pub_thread.start();
        }
        catch(IOException | TikaException | SAXException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Connecting with Broker to match info
     * @param app AppNode that connects to Broker
     * @param broker Broker that accepts the connection
     * @param index index used to set hash of the specific Broker
     * @param message message output to Broker, so the Broker acts accordingly
     */
    public void initConxToBroker(AppNode app, Broker broker, int index, String message) {

        Socket appNodeConnection = null; 
        ObjectOutputStream out = null; 
        ObjectInputStream in = null; ;

        try {
            System.out.println(broker.getIP()+" "+broker.getPort());
            appNodeConnection = new Socket(broker.getIP(), broker.getPort());
            out = new ObjectOutputStream(appNodeConnection.getOutputStream());
            in = new ObjectInputStream(appNodeConnection.getInputStream());

            // sending appropriate message to Broker
            out.writeUTF(message);
            out.flush();

            if (message.equals("Init")) {

                //Reading Broker object
                Message temp = (Message) in.readObject();

                // setting broker to the Brokers list
                int brokerHash = (int) temp.getData();

                // setting broker hash
                brokers.get(index).setHash(brokerHash);
            }
            else if (message.equals("Matching info")) {

                // sending AppNode object to broker
                Message m = new Message(app);
                out.writeObject(m);
                out.flush();
            }
        }
        catch (IOException | ClassNotFoundException ioException) {
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

    /**
     * Publish a new Video
     * @throws TikaException
     * @throws IOException
     * @throws SAXException
     */
    public void publishVideo() throws TikaException, IOException, SAXException {

        File temp_file = new File( System.getProperty("user.dir")+"//temp.txt");
        String txtfile =  temp_file.getParentFile().getParent()+"/videos_dataset";

        // storing any new hashtags
        ArrayList<String> newHashtags = this.channel.addVideo(txtfile,false);

        // matching new hashtags to brokers
        for (String newTag:newHashtags){
            matchHashes(newTag);
        }

        AppNode new_appNode = allBytesToNull();
        ArrayList<Value> channel_videos = new_appNode.getChannel().getChannelVideos();
        Value last_video = channel_videos.get(channel_videos.size()-1);

        // notifying each broker for the new video
        for (Broker broker:brokers){
            notifyBroker(new_appNode, broker,last_video,"Published a new video!");
        }
        System.out.println("\nUploaded successfully video with name: "+last_video.getVideoName());
    }

    /**
     * Delete a Video
     * @throws IOException
     */
    public void deleteVideo() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<Value> channelVideos = channel.getChannelVideos();
        System.out.println("This is the list of all your published videos. Choose the one you want to delete.");
        for (Value video:channelVideos){
            System.out.print(video.getVideoName()+" ");
        }
        System.out.println();

        String video_name  = reader.readLine();
        boolean flag = false;
        Value video_to_del = null;

        for (Value video:channelVideos){
            if (video_name.equals(video.getVideoName())){
                flag = true;
                video_to_del = video;
                break;
            }
        }
        if (!flag){
            System.out.println("Video doesn't exist!");
            return;
        }

        ArrayList<String> hashtagsToBeRemoved = this.channel.removeVideo(video_to_del);

        // removing hashtags from broker match
        for (String hashtag:hashtagsToBeRemoved){
            this.brokerMatch.remove(hashtag);
        }

        AppNode new_appNode = allBytesToNull();
        Value empty_video = (Value) DeepCopy.deepCopy(video_to_del);
        empty_video.setVideoFileChunk(null);

        // notify each broker for deletion
        for (Broker broker:brokers){
            notifyBroker(new_appNode,broker, empty_video,"Deleted a video.");
        }
        System.out.println("\nDeleted successfully video with name: " +video_name);
    }

    /**
     * Notify Broker if a video is deleted or published
     * @param appNode AppNode object
     * @param broker Broker object
     * @param video Video object
     * @param message message output to Broker, so the Broker acts accordingly
     */
    public void notifyBroker(AppNode appNode, Broker broker, Value video, String message){
        Socket appNodeConnection = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            appNodeConnection = new Socket(broker.getIP(), broker.getPort());
            out = new ObjectOutputStream(appNodeConnection.getOutputStream());
            in = new ObjectInputStream(appNodeConnection.getInputStream());

            //sending appropriate message to Broker
            out.writeUTF(message);
            out.flush();

            // sending publisher to broker but with no bytes in his
            // videos, each video has metadata only//but with no bytes in his videos,
            Message m = new Message(appNode);
            out.writeObject(m);
            out.flush();

            // sending video to broker
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

    /**
     * Making a deep copy of AppNode. Also setting all video
     * File chunk values to null to avoid sending whole video
     * in initial connection with brokers, before they make a pull request
     * @return AppNode with null video bytes.
     */
    public AppNode allBytesToNull(){
        AppNode temp = (AppNode) DeepCopy.deepCopy(this);

        Channel temp_channel = temp.getChannel();
        ArrayList<Value> allVideos = temp_channel.getChannelVideos();
        for (Value video: allVideos){
            video.setVideoFileChunk(null);
        }
        return temp;
    }

    /**
     * Update brokerMatch
     * @param key used to update brokerMatch
     */
    private void matchHashes(String key) {

        int keyHash = this.calculateHash(key);

        if (keyHash >= brokers.get(brokers.size() - 1).getHash()) {
            brokerMatch.put(key, brokers.get(0));
            return;
        }
        for (Broker broker : brokers) {
            if (keyHash <= broker.getHash()) {
                brokerMatch.put(key, broker);
                return;
            }
        }
    }

    /**
     * Calculate hash using sha1 function
     * @param key key used to calculate hash
     * @return the calculated hash value
     */
    public int calculateHash(String key) {
        return new BigInteger(DigestUtils.sha1Hex(key), 16).mod(new BigInteger("100")).intValue();
    }

    /**
     * Used for printing purposes
     * @return String representation of AppNode
     */
    public String toString() {
        return "AppNode with IP: " + this.getIP() + " and port: " + this.getPort();
    }

    // GETTERS

    public String getIP() {
        return IP;
    }

    public int getPort() {

        return port;
    }
    public Channel getChannel() {

        return channel;
    }

    public HashMap<String, Broker> getBrokerMatch() {

        return brokerMatch;
    }

    public ArrayList<Broker> getBrokers() {
        return brokers;
    }

    // SETTERS

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setBrokerMatch(){
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBrokers(String path) throws FileNotFoundException {
        File myFile = new File(path);
        Scanner myReader = new Scanner(myFile);
        String line;
        String st[];
        while (myReader.hasNextLine()) {
            line = myReader.nextLine();
            st = line.split(" ");

            // add broker to the list
            brokers.add(new Broker(st[0], Integer.parseInt(st[1])));
        }
    }

    // Main function
    public static void main(String args[]) throws IOException, TikaException, SAXException {

        AppNode  appNode= new AppNode();
    }
}
