import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class to represent a thread for keeping the publisher server up
 */
public class PubServer extends Thread {

    private AppNode appNode;

    // boolean variable to inform us if the user was a consumer before
    // becoming a publisher
    private boolean consPubExists;

    // Constructor
    public PubServer(AppNode appNode, boolean consPubExists)  {
        this.appNode = appNode;
        this.consPubExists = consPubExists;
    }

    //Run process for the thread to follow
    public void run() {
        try {
            this.openPubServer();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    /**
     * Opening publisher Server and waiting for
     * Broker request
     * @throws IOException
     */
    private void openPubServer() throws IOException {

        ServerSocket pubSocket = new ServerSocket(this.appNode.getPort());

        try {
            // If the user was already a consumer, don't create a new
            // thread, because it already exists
            if(this.consPubExists == false){

                File temp_file = new File( System.getProperty("user.dir")+"//temp.txt");
                String txtfile =  temp_file.getParentFile().getParent()+"/videos_dataset";
                Thread cons_thread = new ConsAndPubActions(this.appNode,true,txtfile);

                cons_thread.start();
            }
            while (true) {
                // waiting for a broker to connect
                Socket connection = pubSocket.accept();

                System.out.println("[Acting as a Publisher] Received Broker Request...");

                // Actions for Brokers thread
                Thread t = new ActionsForBrokers(connection, this.appNode);
                t.start();
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {
                pubSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}