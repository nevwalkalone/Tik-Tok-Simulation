package com.example.distrapp.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.distrapp.R;


public class MenuActivity extends AppCompatActivity {

    public MenuActivity(){

    }

    private Button galleryBtn;
    private Button topicsBtn;
    private String username;
    private String port;
    private AlertDialog POPUP_ACTION;
    public static ProgressDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MANAGE_DOCUMENTS,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        galleryBtn = findViewById(R.id.id_my_gallery);
        topicsBtn = findViewById(R.id.id_check_all_topics);

        username = this.getIntent().getStringExtra("USERNAME");

        if (!(username == null)){
            Toast.makeText(this,"LOGGED IN", Toast.LENGTH_SHORT).show();
        }

        if(new LoggedInUser().areNewVideos()){
            Toast.makeText(this,"NEW VIDEOS FROM SUBSCRIPTIONS. CHECK YOUR SAVED VIDEOS", Toast.LENGTH_SHORT).show();
            new LoggedInUser().setNewVideos(false);
        }

        port = this.getIntent().getStringExtra("PORT");

    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onStart() {
        super.onStart();

        galleryBtn.setOnClickListener(view -> {
            POPUP_ACTION = showPopUp(R.layout.videos_popup, R.id.id_saved, R.id.id_published);
            POPUP_ACTION.show();


        });
        topicsBtn.setOnClickListener(view -> {
            onTopicsList();


    });
    }

    public AlertDialog showPopUp(int layoutId, int btn1, int btn2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customLayout = getLayoutInflater().inflate(layoutId, null);

        builder.setView(customLayout);
        AlertDialog dialog = builder.create();

        TextView textMsg = (TextView) customLayout.findViewById(R.id.action_message);

        //published vids button
        Button savedVidsBtn = (Button) customLayout.findViewById(btn1);

        //saved vids button
        Button publishedVidsBtn = (Button) customLayout.findViewById(btn2);

        publishedVidsBtn.setOnClickListener(v->onPublishedSavedVids(false));
        savedVidsBtn.setOnClickListener(v->onPublishedSavedVids(true));

        return dialog;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void onTopicsList(){
        if (RequestTask.firstRequest){

            new RequestTask(this,"random").execute("r");
            Intent intent = new Intent(this,TopicsListActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this,TopicsListActivity.class);
            startActivity(intent);
        }
    }

    public void onPublishedSavedVids(boolean savedVids) {
        POPUP_ACTION.dismiss();
        dialog = ProgressDialog.show(this, "",
                "Just a moment", true);
        dialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialog.dismiss();
            }
        }, 800);
        Intent intent = new Intent(this, Published_SavedVidsActivity.class);
        if (savedVids){
            intent.putExtra("SAVED VIDS","saved");
        }
        startActivity(intent);
    }

}