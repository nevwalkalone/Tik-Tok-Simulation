package com.example.distrapp.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.EditText;

import com.example.distrapp.R;


import java.io.IOException;

public class LogInActivity extends AppCompatActivity {

    private Button loginBtn;
    private String device_ip;
    private EditText userName_text;
    private EditText port_text;
    private boolean isEmulator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        isEmulator = Build.HARDWARE.contains("ranchu");

        loginBtn = findViewById(R.id.complete_login);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        device_ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        loginBtn.setOnClickListener(v->onMenuActivity());
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onMenuActivity(){

        userName_text = findViewById(R.id.login_input_username);
        port_text = findViewById(R.id.port_text);
        String userName = userName_text.getText().toString();
        String portNumber = port_text.getText().toString();

        int port_int = Integer.parseInt(portNumber);


        try {
            boolean newUser = true;
            String[] list = getAssets().list("");
            for (String file: list){
                if (file.equals(userName)) {
                    newUser = false;
                    break;
                }
            }
            LoggedInUser loggedInUser = new LoggedInUser();
            loggedInUser.setIsEmulator(isEmulator);
            loggedInUser.setIp(device_ip);
            loggedInUser.setPort(port_int);
            loggedInUser.setUsername(userName);
            loggedInUser.setNewUser(newUser);

            InitChannelTask task = new InitChannelTask(getApplicationContext(),device_ip,port_int, isEmulator, userName,newUser);
            String [] params = new String[1];
            params[0]="First Initialization";
            task.execute(params);


        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(LogInActivity.this , MenuActivity.class);
        intent.putExtra("USERNAME",userName);
        intent.putExtra("PORT", portNumber);
        startActivity(intent);

    }

}