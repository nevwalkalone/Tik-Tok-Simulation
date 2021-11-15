package com.example.distrapp.view;


import com.example.distrapp.phase1Code.AppNode;


/**
 * Helper class with some static variables
 */

public class LoggedInUser {
    private static AppNode appNode;
    private static boolean isEmulator;
    private static String ip;
    private static int  port;
    private static String username;
    private static boolean newUser;
    private static boolean newVideos = false;



    public static boolean isNewUser() {
        return newUser;
    }
    public static boolean isEmulator() {
        return isEmulator;
    }

    public static String getIp() {
        return ip;
    }

    public static int getPort() {
        return port;
    }

    public static String getUsername() {
        return username;
    }

    public static boolean isNewVideos() {
        return newVideos;
    }

    public LoggedInUser(){

    }

    public void setAppNode(AppNode appNode){
        LoggedInUser.appNode = appNode;
    }


    public AppNode getAppNode(){
        return appNode;
    }


    public void setNewUser(boolean newUser) {
        LoggedInUser.newUser = newUser;
    }

    public void setIsEmulator(boolean isEmulator) {
        LoggedInUser.isEmulator = isEmulator;
    }

    public boolean areNewVideos() {
        return newVideos;
    }

    public void setNewVideos(boolean newVideos) {
        LoggedInUser.newVideos= newVideos;
    }

    public void setIp(String ip){
        LoggedInUser.ip = ip;
    }

    public void setPort(int port){
        LoggedInUser.port = port;
    }

    public void setUsername(String username){
        LoggedInUser.username = username;
    }

}
