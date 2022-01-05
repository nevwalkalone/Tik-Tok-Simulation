import java.io.*;
import java.net.Socket;
import java.util.*;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Class to represent consumer and publisher actions
 */
public class ConsAndPubActions extends Thread {

    public List savedVideos = new ArrayList();
    public AppNode appNode;
    private boolean hasVideos;
    public HashMap<String, Broker> HashInfo = new HashMap<>();
    private boolean firstRequest = true;

    // path with all videos
    private String path;

    // topics this consumer is subbed to
    public HashMap<String, ArrayList<Value>> subbedTopics = new HashMap<>();

    // Constructor
    public ConsAndPubActions(AppNode appNode, boolean hasVideos, String path) {
        this.appNode = appNode;
        this.hasVideos =hasVideos;
        this.path = path;
    }

    /**
     * Run process for the thread to follow
     */
    public void run()  {
        Socket consumerConnection = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            File file = new File(path +"/"+this.appNode.getChannel().getChannelName()+"-cons");
            file.mkdir();

            ArrayList<Integer> ports = new ArrayList<>();
            for (Broker brok : appNode.getBrokers()){
                ports.add(brok.getPort());
            }
            Collections.sort(ports);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Scanner scan = new Scanner(System.in);
            while (true) {
                System.out.println("1.[Acting as a Consumer] Request a topic:");
                System.out.println("2.[Acting as a Consumer] Subscribe to a topic (hashtag or channel)");
                System.out.println("3.[Acting as a Consumer] Refresh.");
                System.out.println("4.[Acting as a Publisher] Publish a new video.");
                System.out.println("5.[Acting as a Publisher] Delete a video.");
                System.out.println("6.Exit.");
                int choice = scan.nextInt();

                Message message;

                if (choice == 1) {

                    // read topic
                    String topic = reader.readLine();

                    //connect to a random broker
                    if(firstRequest){
                        firstRequest = false;
                        //Random connection with a broker
                        double randomNumber = Math.random()*3;
                        int rand_number = (int) randomNumber;
                        rand_number = 12*rand_number;
                        consumerConnection = new Socket(appNode.getIP(), ports.get(0) + rand_number);
                        out = new ObjectOutputStream(consumerConnection.getOutputStream());
                        in = new ObjectInputStream(consumerConnection.getInputStream());

                        // First request.
                        out.writeUTF("First request.");
                        out.flush();

                        // reading hashmap from broker
                        message = (Message) in.readObject();
                        this.HashInfo = (HashMap<String, Broker>) message.getData();

                        // Thread for checking subscriptions and refreshing in general
                        Thread ref_thread = new RefreshVids(this, this.appNode.getChannel().getChannelName(),path);
                        ref_thread.start();
                    }
                    Broker newBroker = null;
                    boolean flag = false;
                    for (String key : this.HashInfo.keySet()) {
                        if (topic.equals(key)) {
                            newBroker = this.HashInfo.get(key);
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        System.out.println("Topic was not found! Try refreshing.");
                        System.out.println("Press any key to continue.");
                        reader.readLine();
                        continue;
                    }

                    // new connection with correct broker
                    consumerConnection = new Socket(newBroker.getIP(), newBroker.getPort());
                    out = new ObjectOutputStream(consumerConnection.getOutputStream());
                    in = new ObjectInputStream(consumerConnection.getInputStream());

                    out.writeUTF("Connected to correct Broker.");
                    out.flush();

                    // sending topic
                    out.writeUTF(topic);
                    out.flush();

                    AppNode withNullVideos = this.appNode.allBytesToNull();

                    // sending appNode
                    Message appNode = new Message(withNullVideos);
                    out.writeObject(appNode);
                    out.flush();


                    String ok_message = in.readUTF();
                    if (ok_message.equals("Key not found")){
                        System.out.println("Topic was not found! Try refreshing.");
                        System.out.println("Press any key to continue.");
                        reader.readLine();
                        continue;
                    }

                    // reading the number of appnodes
                    Message appNodeNumber=(Message) in.readObject();
                    int number_of_apps = (int) appNodeNumber.getData();

                    if (number_of_apps == 0) {
                        System.out.println("No videos received because you are the only one that \n" +
                                "has published videos linked with the specific topic");
                    }

                    for (int k=0; k<number_of_apps; k++) {

                        ok_message = in.readUTF();
                        if (ok_message.equals("Key not found")){
                            continue;
                        }
                        // reading the number of videos
                        Message message2 = (Message) in.readObject();
                        int totalVideos = (int) message2.getData();

                        for (int i = 0; i < totalVideos; i++) {

                            // reading video_name
                            String video_name = in.readUTF();

                            // add video_name to savedVideos
                            if (!savedVideos.contains(video_name)) {
                                savedVideos.add(video_name);
                                out.writeUTF("Send it.");
                                out.flush();
                            }
                            else{
                                // Already got the video saved.
                                out.writeUTF("Don't send it.");
                                out.flush();
                                continue;
                            }
                            message = (Message) in.readObject();
                            int totalChunks = (int) message.getData();

                            // saving the mp4 video locally
                            OutputStream chunkToFile = new FileOutputStream(path +"/"+this.appNode.getChannel().getChannelName()+"-cons/"+ video_name);

                            for (int j = 0; j < totalChunks; j++) {

                                Message temp = (Message) in.readObject();
                                byte[] chunk = (byte[]) temp.getData();
                                chunkToFile.write(chunk);
                            }
                            System.out.println("[Consumer] Received video with name: " +video_name+"" +
                                    " because you made a request for the topic : "+topic);
                        }
                    }
                    try {
                        in.close();
                        out.close();
                        consumerConnection.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(choice == 2){

                    String topic = reader.readLine();


                    // connect to a random broker
                    if(firstRequest){
                        firstRequest = false;

                        // Random connection with a broker
                        double randomNumber = Math.random()*3;
                        int rand_number = (int) randomNumber;
                        rand_number = 12*rand_number;
                        consumerConnection = new Socket(appNode.getIP(), ports.get(0) + rand_number);
                        out = new ObjectOutputStream(consumerConnection.getOutputStream());
                        in = new ObjectInputStream(consumerConnection.getInputStream());

                        // First request.
                        out.writeUTF("First request.");
                        out.flush();

                        // reading hashmap from broker
                        message = (Message) in.readObject();
                        this.HashInfo = (HashMap<String, Broker>) message.getData();

                        // Thread for checking subscriptions and refreshing in general
                        Thread ref_thread = new RefreshVids(this, this.appNode.getChannel().getChannelName(),path);
                        ref_thread.start();
                    }

                    // can't subscribe to himself
                    if (topic.equals(appNode.getChannel().getChannelName())){
                        System.out.println("You can't subscribe to yourself");
                        System.out.println("Press any key to continue.");
                        reader.readLine();
                        continue;
                    }

                    // topic to be subscribed
                    boolean newFlag = false;
                    Broker toBeconnected = null;
                    for (String key : this.HashInfo.keySet()) {
                        if (topic.equals(key)) {
                            toBeconnected = this.HashInfo.get(key);
                            newFlag = true;
                            break;
                        }
                    }
                    if (newFlag == false) {
                        System.out.println("Topic was not found! Refreshing is enabled to every 20 seconds");
                        System.out.println("Press any key to continue.");
                        reader.readLine();
                        continue;
                    }

                    if (this.subbedTopics.containsKey(topic)){
                        System.out.println ("You are already subscribed to this topic.");
                        System.out.println("Press any key to continue.");
                        reader.readLine();
                        continue;
                    }

                    // new connection with correct broker
                    consumerConnection = new Socket(toBeconnected.getIP(), toBeconnected.getPort());
                    out = new ObjectOutputStream(consumerConnection.getOutputStream());
                    in = new ObjectInputStream(consumerConnection.getInputStream());

                    out.writeUTF("Subscription to topic.");
                    out.flush();

                    // sending topic to be subscribed
                    out.writeUTF(topic);
                    out.flush();

                    AppNode withNullVideos = this.appNode.allBytesToNull();

                    // sending appNode
                    Message sub = new Message(withNullVideos);
                    out.writeObject(sub);
                    out.flush();

                    // reading values as objects (NO BYTES HAVE BEEN SENT)
                    // associated with this topic
                    Message new_values = (Message) in.readObject();
                    ArrayList<Value> new_videos = (ArrayList<Value>) new_values.getData();


                    String ok_message = in.readUTF();
                    if (ok_message.equals("Key not found")){
                        System.out.println("Topic was not found! Try refreshing.");
                        System.out.println("Press any key to continue.");
                        reader.readLine();
                        continue;
                    }

                    // reading the number of appnodes
                    Message appNodeNumber=(Message) in.readObject();
                    int number_of_apps = (int) appNodeNumber.getData();

                    this.subbedTopics.put(topic,new_videos);

                    if (number_of_apps == 0) {
                        System.out.println("No videos received because you are the only one that \n" +
                                "has published videos linked with the specific topic");
                        continue;
                    }

                    int totalVideosReceived = 0;
                    for (int k=0; k<number_of_apps; k++) {
                        // reading verification message
                        ok_message = in.readUTF();
                        if (ok_message.equals("Key not found")){
                            continue;
                        }

                        // reading the number of videos
                        Message message2 = (Message) in.readObject();
                        int totalVideos = (int) message2.getData();

                        for (int i = 0; i < totalVideos; i++) {

                            // reading video_name
                            String video_name = in.readUTF();

                            // add video_name to savedVideos
                            if (!savedVideos.contains(video_name) && !appNode.getChannel().getPublishedVideoNames().contains(video_name)) {
                                savedVideos.add(video_name);
                                totalVideosReceived++;
                                out.writeUTF("Send it.");
                                out.flush();
                            }
                            else{
                                // Already got the video saved or published.
                                out.writeUTF("Don't send it.");
                                out.flush();
                                continue;
                            }

                            message2 = (Message) in.readObject();
                            int totalChunks = (int) message2.getData();

                            // saving the mp4 video locally
                            OutputStream chunkToFile = new FileOutputStream(path +"/"+this.appNode.getChannel().getChannelName()+"-cons/"+ video_name);

                            for (int j = 0; j < totalChunks; j++) {

                                Message temp = (Message) in.readObject();
                                byte[] chunk = (byte[]) temp.getData();
                                chunkToFile.write(chunk);
                            }

                            System.out.println("[Consumer] Received video with name: " +video_name+"" +
                                    " because you JUST subscribed to topic : "+topic);
                        }
                    }
                    if (totalVideosReceived == 0) {
                        System.out.println("No videos received because you have either published or saved all videos that are related to this topic");
                    }
                }
                // Refresh choice
                else if (choice == 3){

                    // random connection with a broker
                    Random rand = new Random();
                    int rand_number = rand.nextInt(3);
                    rand_number = 15*rand_number;
                    consumerConnection = new Socket(appNode.getIP(), ports.get(0)+ rand_number);
                    out = new ObjectOutputStream(consumerConnection.getOutputStream());
                    in = new ObjectInputStream(consumerConnection.getInputStream());

                    // Refreshing
                    out.writeUTF("Refresh for new videos.");
                    out.flush();

                    // reading hashmap from broker
                    message = (Message) in.readObject();
                    this.HashInfo = (HashMap<String, Broker>) message.getData();
                    System.out.println("Refreshed!");
                }

                // Publish video choice
                else if (choice==4) {
                    if (hasVideos==false){
                        System.out.println("This is the first video you publish!");
                        this.appNode.init(true);
                        hasVideos = true;
                    }
                    else{
                        // already a publisher
                        this.appNode.publishVideo();
                    }
                }
                // Delete video choice
                else if (choice==5){
                    if (hasVideos==false){
                        System.out.println("You don't have any videos to delete!");
                    }
                    else{
                        this.appNode.deleteVideo();
                    }
                }
                // Exit choice
                else{
                    System.out.println("Publish and request actions will not be available from now on. Only accepting broker requests if the user is already a publisher.");
                    reader.close();
                    scan.close();
                    break;
                }
                System.out.println("Press any key to continue.");
                reader.readLine();
            }
        } catch (IOException | ClassNotFoundException | TikaException | SAXException ioException) {
            ioException.printStackTrace();
        }
    }
}