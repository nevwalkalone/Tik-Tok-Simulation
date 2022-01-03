import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Class to represent the channel of a producer
 */
public class Channel implements Serializable {

    public String channelName;
    private Set<String> hashtagsPublished = new HashSet<>();
    private ArrayList<Value> channelVideos = new ArrayList<>();
    private ArrayList<String> publishedVideoNames = new ArrayList<>();

    // Hashmap storing videos linked with a specific topic
    private HashMap<String, ArrayList<Value>> userVideoFilesMap = new HashMap<>();

    private static final long serialVersionUID = -2723363051271966964L;

    // Constructors
    public Channel(){}
    public Channel(String channelName){
        this.channelName = channelName;
    }

    /**
     * Setting up all info for the channel
     * @param path path that contains producers videos
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     */
    public void setUpChannel(String path) throws IOException, TikaException, SAXException {

        // adding each video
        File myFile = new File (path+"/"+this.channelName+"/Hashtags-For-EachVideo.txt");
        Scanner myReader = new Scanner(myFile);
        String line;
        String st[];

        while (myReader.hasNextLine()){
            // creating a new Video
            Value value = new Value();
            // setting channel name for specific video
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
                    publishedVideoNames.add(string);
                }
            }
            // adding hashtags to the video
            value.setAssociatedHashtags(hashtContain);

            // adding hashtags of video to hashtags of the channel
            this.addChannelHashtags(hashtContain);

            // adding to hashmap new hashtags and video
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
            // metadata extraction
            this.extractMetaData(path+"/"+this.channelName+"/"+value.getVideoName(),value);

            // adding video to channel's videos
            this.addChannelVideos(value);
        }
    }

    /**
     * Adding a new video and updating all structures appropriately
     * @param path path that contains producers videos
     * @param firstVideo boolean value to inform us if this is the first video of the channel
     * @return new hashtags to add them to brokermatch hashmap
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     */
    public ArrayList<String> addVideo(String path, boolean firstVideo) throws IOException, TikaException, SAXException {

        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Creating a directory if this is the first video
        if(firstVideo == true){
            File file = new File(path +"/"+this.channelName );
            file.mkdir();
        }

        System.out.println("Give path of video to be uploaded.");
        //System.out.println("Press any key after uploading the video.");
        String video_path = reader.readLine();
        File source = new File(video_path);
        String[] to_find_name = video_path.split("\\\\");
        String videoName = to_find_name[to_find_name.length-1];
        publishedVideoNames.add(videoName);

        System.out.println("Please give additional info for the video.");
        System.out.println("Hashtags of the video:");
        String temp = reader.readLine();
        String hashtags[] = temp.split(" ");
        String new_path = path+"/"+this.channelName+"/Hashtags-For-EachVideo.txt";

        File file2 = new File(new_path);

        FileWriter fw = new FileWriter(file2,true);

        if (firstVideo == false){
            fw.write("\n");
        }
        for (int i = 0; i <= hashtags.length; i++) {
            if ( i==0){
                fw.write(videoName+" ");
            }
            else{
                //this.hashtags_of_latest_video.add(hashtags[i-1]);
                fw.write(hashtags[i-1]+" ");
            }
        }
        fw.close();

        // creating a new Video
        Value value = new Value();

        // setting channel name for specific video
        value.setChannelName(this.channelName);
        value.setVideoName(videoName);

        ArrayList<String> hashtContain = new ArrayList<>();

        for (String string:hashtags){
            hashtContain.add(string);
        }

        // adding hashtags to the video
        value.setAssociatedHashtags(hashtContain);

        // adding hashtags of video to hashtags of the channel
        this.addChannelHashtags(hashtContain);

        // hashtags to be returned
        ArrayList<String> newHashtags = new ArrayList<>();

        // adding to hashmap new hashtags and video
        for (String hashtag:hashtContain) {
            if (userVideoFilesMap.containsKey(hashtag)) {
                userVideoFilesMap.get(hashtag).add(value);
            }
            else{
                ArrayList<Value> tempor = new ArrayList<>();
                tempor.add(value);
                userVideoFilesMap.put(hashtag,tempor);
                newHashtags.add(hashtag);
            }
        }

        // copying video to the producer's directory
        File dest = new File(path+"/"+this.channelName+"/"+value.getVideoName());
        FileUtils.copyFile(source, dest);

        // extract Meta Data
        this.extractMetaData(path+"/"+this.channelName+"/"+value.getVideoName(),value);

        // adding video to channel's videos
        this.addChannelVideos(value);

        // returning new hashtags to add them to brokermatch hashmap
        return newHashtags;
    }

    /**
     * Removing a video and updating all structures appropriately
     * @param video_to_del Video to be removed
     * @return ArrayList of hashtags to be removed
     */
    public ArrayList<String> removeVideo(Value video_to_del){
        ArrayList<Value> channelVideos = this.getChannelVideos();

        // removing video from channelVideos
        channelVideos.remove(video_to_del);

        publishedVideoNames.remove(video_to_del.getVideoName());

        // video hashtags
        ArrayList<String> video_tags = video_to_del.getAssociatedHashtags();

        // all hashtags
        Set<String> hashtagsPublished = this.getHashtagsPublished();

        // hashtags returning to appnode so they are removed
        // from brokermatch hashmap
        ArrayList<String> hashtagsToBeRemoved = new ArrayList<>();
        HashMap<String, ArrayList<Value>> videoFileMap = this.getVideoFiles();

        for (String hashtag:video_tags){
            // videos containing the specific hashtag
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
     * Meta Data extraction for specific video
     * @param path path of video to be extracted
     * @param video video to be extracted
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     */
    public void extractMetaData(String path, Value video) throws IOException, TikaException, SAXException {

        // detecting the file type
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(new File(path));
        ParseContext pcontext = new ParseContext();

        // Html parser
        MP4Parser MP4Parser = new MP4Parser();
        MP4Parser.parse(inputstream, handler, metadata,pcontext);
        String[] metadataNames = metadata.names();

        for(String name : metadataNames) {
            if( name.equals("date")){
                video.setDateCreated(metadata.get(name));
            }
            else if(name.equals("tiff:ImageLength")){
                video.setFrameHeight(metadata.get(name));
            }
            else if(name.equals("tiff:ImageWidth")){
                video.setFrameWidth(metadata.get(name));
            }
        }
        File file = new File(path);
        BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
        video.setLength((int) file.length());
        byte[] bytes = new byte[(int) file.length()];
        fileInputStream.read(bytes);
        video.setVideoFileChunk(bytes);
        fileInputStream.close();
    }

    /**
     * Dividing a video into chunks
     * @param bytes Bytes to be divided
     * @return Arraylist of chunks
     */
    public ArrayList<byte[]> makeChunks(byte[] bytes) {

        // 0.5 mb each chunk
        int chunkSize = 512 * 1024;
        ArrayList<byte[]> chunks = new ArrayList<>();
        //System.out.println(bts.length % blockSize);

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

    // Getters
    public ArrayList<String> getPublishedVideoNames() {
        return publishedVideoNames;
    }

    public Set<String> getHashtagsPublished() {

        return hashtagsPublished;
    }

    public ArrayList<Value> getChannelVideos() {

        return channelVideos;
    }

    public String getChannelName() {

        return channelName;
    }

    public HashMap<String, ArrayList<Value>> getVideoFiles() {

        return userVideoFilesMap;
    }

    /**
     * Add new hastags to channel's hashtag
     * @param hashtags Hashtags to be added
     */
    public void addChannelHashtags(ArrayList<String> hashtags){
        for (String tag:hashtags){
            this.getHashtagsPublished().add(tag);
        }
    }

    /**
     * Add a video to channel's videos
     * @param value Video to be added
     */
    public void addChannelVideos(Value value) {

        this.channelVideos.add(value);
    }
}
