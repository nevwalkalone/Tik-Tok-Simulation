import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to represent a thread that is responsible for actions
 * related to publisher, triggered by broker
 */
public class ActionsForPublishers extends Thread {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String message;
    private Broker broker;

    //Constructor
    public ActionsForPublishers (String message, ObjectOutputStream out, ObjectInputStream in, Broker broker){
        this.out = out;
        this.in = in;
        this.message = message;
        this.broker = broker;
    }

    //Run process to follow
    public void run(){
        try {
            if (message.equals("Init")) {

                System.out.println("Received INITIAL AppNode connection.");

                //sending broker hash
                //to publisher
                Message message = new Message(broker.getHash());
                out.writeObject(message);
                out.flush();

            }
            else if (message.equals("Matching info"))  {

                // Reading AppNode Object
                Message temp = (Message) in.readObject();

                AppNode newAppNode = (AppNode) temp.getData();
                System.out.println(newAppNode);

                HashMap<String, ArrayList<Value>> tempForSubs = newAppNode.getChannel().getVideoFiles();

                synchronized (broker){
                    // add appnodes to the list
                    broker.addAppnode(newAppNode);
                    //make HashMaps
                    broker.makeHashMaps(newAppNode);
                }
            }
            else if (message.equals("Published a new video!")){
                System.out.println("An AppNode published a new video. Updating hashmaps.");

                // Reading AppNode Object
                Message temp = (Message) in.readObject();
                AppNode newAppNode = (AppNode) temp.getData();

                // Reading Video to be published
                Message m = (Message) in.readObject();

                Value video_to_pub = (Value) m.getData();

                synchronized (broker){
                    broker.updateHashMaps(newAppNode,video_to_pub,"publish");
                }
            }
            else{
                System.out.println("An AppNode deleted a video. Updating hashmaps.");

                // Reading AppNode Object
                Message temp = (Message) in.readObject();
                AppNode newAppNode = (AppNode) temp.getData();

                // Reading Video to be deleted
                Message m = (Message) in.readObject();
                Value video_to_del = (Value) m.getData();

                synchronized(broker){
                    // update hashmaps
                    broker.updateHashMaps(newAppNode,video_to_del,"delete");
                }

            }
        }
        catch (IOException | ClassNotFoundException ioException) {
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




