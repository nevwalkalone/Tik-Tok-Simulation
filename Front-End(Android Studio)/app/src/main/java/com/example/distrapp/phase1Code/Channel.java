package com.example.distrapp.phase1Code;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;


import androidx.annotation.RequiresApi;

import com.example.distrapp.view.LoggedInUser;

import java.io.*;
import java.util.*;


/**
 * CLASS TO REPRESENT THE CHANNEL OF A PRODUCER
 **/

public class Channel implements Serializable {

    public String channelName;

    public transient AssetManager mngr;

    private Set<String> hashtagsPublished = new HashSet<>();

    private ArrayList<Value> channelVideos = new ArrayList<>();

    private ArrayList<Value> consumedVideos = new ArrayList<>();

    //Hashmap storing videos linked with a specific topic
    private HashMap<String, ArrayList<Value>> userVideoFilesMap = new HashMap<>();

    private static final long serialVersionUID = -2723363051271966964L;


    //Constructors
    public Channel(AssetManager mngr){
        this.mngr = mngr;
    }

    public Channel(String channelName){

        this.channelName = channelName;
    }

    /**
     * Setting up all info for the channel
     **/
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void setUpChannel(Context myContext) throws IOException {

        //reading config file containing hashtags for each video
        Scanner myReader = new Scanner(new InputStreamReader(mngr.open(this.channelName+"/Hashtags-For-EachVideo.txt")));
        String line;
        String st[];

        while (myReader.hasNextLine()){
            //creating a new Video
            Value value = new Value();
            //setting channel name for specific video
            value.setChannelName(this.channelName);
            ArrayList<String> hashtContain = new ArrayList<>();

            line = myReader.nextLine();
            st = line.split(" ");

            for (String string:st){
                if (string.contains("#")){
                    hashtContain.add(string);
                }
                else{
                    value.setVideoName(string);
                }
            }
            //adding hashtags to the video
            value.setAssociatedHashtags(hashtContain);

            //adding hashtags of video
            //to hashtags of the channel
            this.addChannelHashtags(hashtContain);

            //adding to hashmap
            //new hashtags and video
            for (String hashtag:hashtContain) {
                if (userVideoFilesMap.containsKey(hashtag)) {
                    userVideoFilesMap.get(hashtag).add(value);
                }
                else{
                    ArrayList<Value> temp = new ArrayList<>();
                    temp.add(value);
                    userVideoFilesMap.put(hashtag,temp);
                }
            }

            //saving videos locally to emulator or device
            this.localSave(value, myContext);

            //metadata extraction
            this.extractMetaData(value, myContext, "Publish");

            //adding video
            //to channel's videos
            this.addChannelVideos(value);
        }
    }

    /**
     * Adding a new video
     * and updating all structures
     * appropriately
     **/
    @RequiresApi(api = Build.VERSION_CODES.P)
    public ArrayList<String> addVideo(Context myContext, Uri uri, String videoName, String hashtags)  {

        //save file to local storage
        try {
            InputStream is = myContext.getContentResolver().openInputStream(uri);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            File file;

            if (LoggedInUser.isEmulator()){
                File folderFile = new File(myContext.getExternalFilesDir(null).getAbsolutePath() + File.separator+"Publish");
                folderFile.mkdir();
                file = new File(folderFile.getAbsolutePath() + File.separator + videoName);
            }
            else{
               file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator + "Publish" + File.separator + videoName);
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);
            outputStream.close();

            //creating a new Video
            Value value = new Value();

            //setting channel name for specific video
            value.setChannelName(this.channelName);
            value.setVideoName(videoName);

            String[] newTags = hashtags.split(",");


            ArrayList<String> hashtContain = new ArrayList<>(Arrays.asList(newTags));

            //adding hashtags to the video
            value.setAssociatedHashtags(hashtContain);

            //adding hashtags of video
            //to hashtags of the channel
            this.addChannelHashtags(hashtContain);

            //hashtags to be returned
            ArrayList<String> newHashtags = new ArrayList<>();

            //adding to hashmap
            //new hashtags and video
            for (String hashtag : hashtContain) {
                if (userVideoFilesMap.containsKey(hashtag)) {
                    userVideoFilesMap.get(hashtag).add(value);
                } else {
                    ArrayList<Value> tempor = new ArrayList<>();
                    tempor.add(value);
                    userVideoFilesMap.put(hashtag, tempor);
                    newHashtags.add(hashtag);
                }
            }

            //metadata extraction
            this.extractMetaData(value, myContext, "Publish");

            //adding video
            //to channel's videos
            this.addChannelVideos(value);

            //returning new hashtags to add them to brokermatch hashmap
            return newHashtags;

        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //saving videos to local storage of device
    public void localSave(Value value, Context myContext) throws IOException {

        InputStream is = mngr.open(value.getChannelName() + File.separator + value.getVideoName());
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        if (LoggedInUser.isEmulator()) {

            File folderFile = new File(myContext.getExternalFilesDir(null).getAbsolutePath() + File.separator + "Publish");
            folderFile.mkdir();
            File myFile = new File(folderFile.getAbsolutePath() + File.separator + value.getVideoName());
            FileOutputStream outputStream = new FileOutputStream(myFile);
            outputStream.write(buffer);
            outputStream.close();
        }
        else {
            File folderFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator + "Publish");
            folderFile.mkdir();
            File myFile = new File(folderFile.getAbsolutePath() + File.separator + value.getVideoName());

            FileOutputStream outputStream = new FileOutputStream(myFile);
            outputStream.write(buffer);

            outputStream.close();
        }
    }

    /**
     * Removing a video
     * and updating all structures
     * appropriately
     **/
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<String> removeVideo(Value video_to_del){

        ArrayList<Value> channelVideos = this.getChannelVideos();

        //removing video from channelVideos
        channelVideos.remove(video_to_del);

        //video hashtags
        ArrayList<String> video_tags = video_to_del.getAssociatedHashtags();

        //all  hashtags
        Set<String> hashtagsPublished = this.getHashtagsPublished();

        //hashtags returning to appnode
        //so they are removed from brokermatch
        //hashmap
        ArrayList<String> hashtagsToBeRemoved = new ArrayList<>();

        HashMap<String, ArrayList<Value>> videoFileMap = this.getVideoFiles();

        for (String hashtag:video_tags){
            //videos containing the specific hashtag
            ArrayList<Value> hashtag_vids = this.getVideoFiles().get(hashtag);

            if(hashtag_vids.size() == 1){
                videoFileMap.remove(hashtag);
                hashtagsPublished.remove(hashtag);
                hashtagsToBeRemoved.add(hashtag);
            }
            else{
                hashtag_vids.remove(video_to_del);
                videoFileMap.replace(hashtag,hashtag_vids);
            }
        }
        return hashtagsToBeRemoved;
    }

    /**
     *  Meta Data extraction for specific video
     **/

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void extractMetaData(Value video, Context myContext, String specificFolder) throws IOException{
        File myFile;

        if (LoggedInUser.isEmulator()){
            myFile = new File(myContext.getExternalFilesDir(null).getAbsolutePath()+File.separator+specificFolder+File.separator+video.getVideoName());
        }
        else{
            myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath()+File.separator+specificFolder+File.separator+video.getVideoName());

        }

        BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(myFile));
        byte[] bytes = new byte[(int) myFile.length()];


        fileInputStream.read(bytes);
        video.setVideoFileChunk(bytes);
        fileInputStream.close();


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(myFile.getAbsolutePath());

        String date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE).substring(0,8);
        String newDate ="";
        for (int i=0; i<date.length(); i++){
           newDate+=date.charAt(i);
           if ((i==3) || (i==5)){
               newDate+="/";
           }
        }
        video.setDateCreated(newDate);
        video.setFrameWidth(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        video.setFrameHeight(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        video.setLength(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/ 1000);

        if (LoggedInUser.isEmulator()){
            video.setFramerate(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)) / video.getLength());
        }
        else{
            video.setFramerate(30);
        }

    }

    /**
     * Dividing a video into chunks
     **/
    public ArrayList<byte[]> makeChunks(byte[] bytes) {

        //0.5 mb each chunk
        int chunkSize = 512 * 1024;
        ArrayList<byte[]> chunks = new ArrayList<>();

        int chunkNumber = (bytes.length+chunkSize-1) / chunkSize;

        byte[] chunk = null;
        for (int i = 1; i < chunkNumber; i++) {
            int index = (i - 1) * chunkSize;
            chunk = Arrays.copyOfRange(bytes, index, index + chunkSize);
            chunks.add(chunk);
        }
        int end = -1;
        if (bytes.length % chunkSize  == 0) {
            end = bytes.length;
        } else {
            end = bytes.length % chunkSize + chunkSize * (chunkNumber - 1);
        }

        chunk = Arrays.copyOfRange(bytes, (chunkNumber - 1) * chunkSize, end);
        chunks.add(chunk);

        return chunks;
    }

    //-----------------------------------------

    //GETTERS

    public Set<String> getHashtagsPublished() {

        return hashtagsPublished;
    }

    public ArrayList<Value> getChannelVideos() {

        return channelVideos;
    }

    public ArrayList<Value> getConsumedVideos() {
        return consumedVideos;
    }

    public String getChannelName() {

        return channelName;
    }

    public HashMap<String, ArrayList<Value>> getVideoFiles() {

        return userVideoFilesMap;
    }

    //-----------------------------------------

    protected void setChannelName(String channelName){
        this.channelName = channelName;
    }

    /**
     * Add a hashtag to channel's hashtag
     **/
    public void addChannelHashtags(ArrayList<String> hashtags){
        for (String tag:hashtags){
            this.getHashtagsPublished().add(tag);
        }
    }

    /**
     * Add a video to channel's videos
     **/
    public void addChannelVideos(Value value) {

        this.channelVideos.add(value);
    }

    /**
     * Αdd a video to consumed videos
     * @param value the video
     */
    public void addConsumedVideos(Value value) {
        this.consumedVideos.add(value);
    }

}
