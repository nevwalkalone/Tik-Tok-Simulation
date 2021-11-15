package com.example.distrapp.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.distrapp.R;

public class PlayVideoActivity extends AppCompatActivity {
    private boolean showSavedVids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        String path = getIntent().getStringExtra("VIDEO PATH");

        if (getIntent().getStringExtra("SAVED VIDS") != null){
            showSavedVids = true;
        }

        Uri uri =null;

        if (path.equals("uri")){
            uri = Uri.parse(getIntent().getStringExtra("VIDEOURI"));
        }
        else{
            uri = Uri.parse(path);
        }

        VideoView videoView = findViewById(R.id.video_view);
        videoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, Published_SavedVidsActivity.class);
        if (showSavedVids){
            intent.putExtra("SAVED VIDS","saved");
        }
        startActivity(intent);

    }
}