package edu.temple.nfccomponent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    KeyService keyService;
    boolean connected;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KeyService.TestBinder binder = (KeyService.TestBinder) service;
            keyService = binder.getService();
            connected = true;

            keyService.getMyKeyPair();
            keyService.generatePEM();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (connected) {
            keyService.generatePEM();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent= new Intent(this, KeyService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }
}
