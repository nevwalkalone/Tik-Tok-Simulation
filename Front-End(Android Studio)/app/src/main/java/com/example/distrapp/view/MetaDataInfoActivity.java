package com.example.distrapp.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.distrapp.R;
import com.example.distrapp.phase1Code.AppNode;
import com.example.distrapp.phase1Code.Value;

import java.util.ArrayList;

public class MetaDataInfoActivity extends AppCompatActivity {

    private boolean showSavedVids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta_data_info);
        String videoname = getIntent().getStringExtra("VIDEONAME");
        AppNode appNode = new LoggedInUser().getAppNode();

        ArrayList<Value> videos = new ArrayList<>();
        if (getIntent().getStringExtra("SAVED VIDS") != null){
            showSavedVids = true;
            videos = appNode.getChannel().getConsumedVideos();
        }
        else{
            videos = appNode.getChannel().getChannelVideos();
        }

        Value video = new Value();
        for (Value value: videos){
            if (value.getVideoName().equals(videoname)){
                video = value;
                break;
            }
        }
        setVidName(video.getVideoName());
        setDate(video.getDateCreated());
        setHashtags(video.getAssociatedHashtags());
        setLength(video.getLength() +" secs");
        setFrameRate(video.getFramerate());
        setFrameHeight(video.getFrameHeight());
        setFrameWidth(video.getFrameWidth());
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, Published_SavedVidsActivity.class);
        if (showSavedVids){
            intent.putExtra("SAVED VIDS","saved");
        }
        startActivity(intent);

    }

    public void setVidName(String vidName) {
        ((TextView) findViewById(R.id.text_videoname)).setText(vidName);
    }

    public void setHashtags(ArrayList<String> hashtags) {
        StringBuffer sb = new StringBuffer();
        int counter = 0;
        for (String s:hashtags){
            sb.append(s);
            if (counter < hashtags.size()-1){
                sb.append(", ");
            }
            counter++;
        }
        String string = sb.toString();
        ((TextView) findViewById(R.id.text_vid_hashtags)).setText(string);
    }

    public void setDate(String date) {
        ((TextView) findViewById(R.id.text_date_created)).setText(date);
    }

    public void setLength(String length) {
        ((TextView) findViewById(R.id.text_vid_length)).setText(length);
    }

    public void setFrameRate(long frameRate) {
        ((TextView) findViewById(R.id.text_vid_frame_rate)).setText(String.valueOf(frameRate));
    }

    public void setFrameHeight(String frameHeight) {
        ((TextView) findViewById(R.id.text_vid_frame_height)).setText(frameHeight);
    }

    public void setFrameWidth(String frameWidth) {
        ((TextView) findViewById(R.id.text_vid_frame_width)).setText(frameWidth);
    }
}