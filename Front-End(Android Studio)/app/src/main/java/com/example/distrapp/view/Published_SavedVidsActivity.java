package com.example.distrapp.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.distrapp.R;
import com.example.distrapp.phase1Code.AppNode;
import com.example.distrapp.phase1Code.Value;
import com.example.distrapp.view.VideoFragment.VideosListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class Published_SavedVidsActivity extends AppCompatActivity implements VideosListFragment.OnListFragmentInteractionListener{

    private AppNode appNode;
    private FloatingActionButton addBtn;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 2;
    private boolean showSavedVids;
    private VideosListFragment videosListFragment;
    private boolean firstVideo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_published_vids);

        appNode = new LoggedInUser().getAppNode();
       // MenuActivity.dialog.dismiss();

        if (getIntent().getStringExtra("SAVED VIDS") != null){
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null)
            {
                actionBar.setTitle("Saved Videos");
            }
            showSavedVids = true;
            (findViewById(R.id.publish_vid_button)).setVisibility(View.GONE);
        }
        else{
            showSavedVids = false;
            addBtn = findViewById(R.id.publish_vid_button);
            addBtn.setOnClickListener(v ->publishVidPopUp() );
        }

        firstVideo = LoggedInUser.isNewUser();

        if (getVideosList().isEmpty()) {
            ConstraintLayout parentLayout = (ConstraintLayout) findViewById(R.id.constraint_published_vids);
            TextView textView = new TextView(this);
            textView.setText("No videos found.");
            textView.setTextSize(20.0f);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            parentLayout.addView(textView, 0, params);
        }


        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            String path;

            if (showSavedVids){
                path =  getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+File.separator+"Saved"+File.separator;
            }
            else{
                path =  getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+File.separator+"Publish"+File.separator;
            }
            videosListFragment = VideosListFragment.newInstance(1,path);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, videosListFragment)
                    .commit();
        }

    }

    public void watchDelInfoPopUp(String videoName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setTitle("Choose Action");


        //Button One : Yes
        builder.setPositiveButton("Watch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Published_SavedVidsActivity.this, PlayVideoActivity.class);
                String path;
                String specificFolder;
                if (showSavedVids){
                    specificFolder = "Saved";
                }
                else{
                    specificFolder ="Publish";
                }

                if (LoggedInUser.isEmulator()){
                    path =  getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+File.separator+specificFolder+File.separator+videoName;
                }
                else{
                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath()+ File.separator+specificFolder+File.separator+videoName;
                }

                intent.putExtra("VIDEO PATH", path);

                if (showSavedVids){
                    intent.putExtra("SAVED VIDS","saved");
                }
                Toast.makeText( Published_SavedVidsActivity.this, "Play Video", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });


        //Button Two : No
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                deleteVidPopUp(videoName);
            }
        });

        //Button Three : Neutral
        builder.setNeutralButton("Info", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Published_SavedVidsActivity.this,MetaDataInfoActivity.class);
                intent.putExtra("VIDEONAME", videoName);
                if (showSavedVids){
                    intent.putExtra("SAVED VIDS","saved");
                }
                Toast.makeText(Published_SavedVidsActivity.this, "Video Info shown", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
        AlertDialog diag = builder.create();
        diag.show();
    }



    public void deleteVidPopUp(String videoName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setTitle("Choose Action");


        builder.setMessage("Are you sure you want to delete this video?");


        //Button One : Yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (showSavedVids){
                    ArrayList<Value> consumed_vids = appNode.getChannel().getConsumedVideos();
                    for (Value value: consumed_vids){
                        if (value.getVideoName().equals(videoName)){
                            //remove saved video that was selected
                            consumed_vids.remove(value);
                            break;
                        }
                    }
                }
                else{
                    //System.out.println(videoName);
                    String st[] = new String[2];
                    st[0] = videoName;
                    st[1] = "delete";
                    new PublishDeleteTask().execute(st);
                    dialog.cancel();
                }

                if(LoggedInUser.isEmulator()){
                    Toast.makeText( Published_SavedVidsActivity.this, "Video successfully deleted", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                else{
                    Toast.makeText( Published_SavedVidsActivity.this, "Video successfully deleted", Toast.LENGTH_SHORT).show();
                    recreate();
                }
            }
        });

        //Button Two : No
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog diag = builder.create();
        diag.show();
    }


    public void publishVidPopUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");

        //Button One : Yes
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { pickVidFromGallery();

            }
        });

        //Button Two : No
        builder.setNegativeButton("Record", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                startActivityForResult(intent, 1);
            }
        });

        AlertDialog diag = builder.create();
        diag.show();
    }

    public void pickVidFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType(("video/*"));
        startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode ==RESULT_OK && requestCode == REQUEST_TAKE_GALLERY_VIDEO)  || (resultCode == RESULT_OK && requestCode == 1) ){
            Uri uri = data.getData();
            Intent intent = new Intent(this,VideoInfoActivity.class);
            intent.putExtra("VIDEOURI", uri.toString());
            intent.putExtra("firstvideo",firstVideo);
            startActivity(intent);
        }
    }

    /**
     * get the invites that the player has received
     * @return the ArrayList of invites
     */
    @Override
    public ArrayList<Value> getVideosList() {
        if (showSavedVids){
            return appNode.getChannel().getConsumedVideos();
        }
        return appNode.getChannel().getChannelVideos();
    }

    /**
     * when a invitation is selected
     * @param item the invitation
     */
    @Override
    public void onListFragmentInteraction(Value item) {
        watchDelInfoPopUp(item.getVideoName());
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    //used for filtering a list
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_menu,menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                videosListFragment.adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}