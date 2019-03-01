package edu.temple.nfccomponent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.NfcA;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    KeyService keyService;
    boolean connected;
    String userKeyForExchange = null;

    NfcAdapter nfcAdapter;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KeyService.TestBinder binder = (KeyService.TestBinder) service;
            keyService = binder.getService();
            connected = true;

            keyService.getMyKeyPair();
            userKeyForExchange = keyService.getUserPublicForExchange("brendan");
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

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        nfcAdapter.setNdefPushMessageCallback(this, this );
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

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String messageString = userKeyForExchange;
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", messageString.getBytes());
        return new NdefMessage(ndefRecord);
    }

}
