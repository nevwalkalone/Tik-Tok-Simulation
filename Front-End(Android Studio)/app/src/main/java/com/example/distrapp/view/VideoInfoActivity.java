package com.example.distrapp.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.distrapp.R;

public class VideoInfoActivity extends AppCompatActivity {

    private Button publishBtn;
    private Uri uri;
    private boolean firstVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uri = Uri.parse(getIntent().getStringExtra("VIDEOURI"));
        if (!LoggedInUser.isEmulator()){
            setContentView(R.layout.activity_video_info2);
        }
        else{
            setContentView(R.layout.activity_video_info);
            MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
            mMMR.setDataSource(getApplicationContext(), uri);
            Bitmap bitmap = mMMR.getFrameAtTime();
            ImageView image = findViewById(R.id.change_image);
            image.setImageBitmap(bitmap);
        }
        firstVideo = getIntent().getExtras().getBoolean("firstvideo");
        publishBtn = findViewById(R.id.publishBtn);
        publishBtn.setOnClickListener(v->backToMenu());

    }

    public void backToMenu(){
        EditText vidName_text = findViewById(R.id.vid_name_text);
        EditText hashtags_text = findViewById(R.id.hashtags_text);
        String vidName = vidName_text.getText().toString();
        String hashtags = hashtags_text.getText().toString();

        String[] st = new String[4];

        if (firstVideo){
            st[0] = "firstvideo";
            st[1] = vidName;
            st[2] = hashtags;
            //first video so we initialize the channel
            InitChannelTask task = new InitChannelTask(getApplicationContext(),LoggedInUser.getIp(),LoggedInUser.getPort(), LoggedInUser.isEmulator(), LoggedInUser.getUsername(),firstVideo);
            task.setUri(uri);
            task.execute(st);
            new LoggedInUser().setNewUser(false);
        }
        else{
            st[0] = vidName;
            st[1] = "publish";
            st[2] = hashtags;
            new PublishDeleteTask(getApplicationContext(),uri,firstVideo).execute(st);
        }

        Intent intent = new Intent(this, MenuActivity.class);
        Toast.makeText( this, "Video successfully published", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}