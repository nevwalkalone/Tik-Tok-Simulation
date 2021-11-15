package com.example.distrapp.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import com.example.distrapp.R;
import com.example.distrapp.view.TopicFragment.TopicsListFragment;
import java.util.ArrayList;


public class TopicsListActivity extends AppCompatActivity implements TopicsListFragment.OnListFragmentInteractionListener {

    private TopicsListFragment topicsListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_list);

        if (getTopicsList().isEmpty()) {
            ConstraintLayout parentLayout = (ConstraintLayout) findViewById(R.id.constraint_topics_list);
            TextView textView = new TextView(this);
            textView.setText("No Topics found.");
            textView.setTextSize(20.0f);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            parentLayout.addView(textView, 0, params);
        }

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            topicsListFragment = TopicsListFragment.newInstance(1);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, topicsListFragment)
                    .commit();
        }
    }

    @Override
    public void onListFragmentInteraction(String item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        //builder.setTitle("Choose Action");


        builder.setMessage("By subscribing you are going to receive current AND future videos associated with this topic.");

        //Button One : Yes
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new RequestTask(TopicsListActivity.this,item).execute();
            }
        });

        AlertDialog diag = builder.create();
        diag.show();
    }

    @Override
    public ArrayList<String> getTopicsList() {
        return RequestTask.allTopics;
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }


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
               topicsListFragment.adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}