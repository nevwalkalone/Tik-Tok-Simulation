package com.example.distrapp.phase1Code;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;



/**
 * CLASS TO REPRRESENT A THREAD FOR KEEPING THE PUBLISHER
 * SERVER UP
 **/

public class PubServer extends Thread {

    private AppNode appNode;


    public PubServer(AppNode appNode)  {

        this.appNode = appNode;
    }

    //Run process to follow
    public void run() {
        try {
            this.openPubServer();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    /**
     * Opening publisher Server
     * and waiting for broker request
     **/
    private void openPubServer() throws IOException {

        ServerSocket pubSocket = null;
        pubSocket = new ServerSocket(this.appNode.getPort());

        try {

            while (true) {
                //waiting for a broker to connect
                Socket connection = pubSocket.accept();

                System.out.println("[Acting as a Publisher] Received Broker Request...");

                //Actions for Brokers thread
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