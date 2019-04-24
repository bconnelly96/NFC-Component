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
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    KeyService keyService;
    boolean connected;
    String userKeyForExchange = null;
    //TODO: in final lab implement MapFragment interface to receive selected partner's userName
    String partnerPublicKeyString = null;

    NfcAdapter nfcAdapter;

    TextView textView;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KeyService.TestBinder binder = (KeyService.TestBinder) service;
            keyService = binder.getService();
            connected = true;

            keyService.getMyKeyPair();
            userKeyForExchange = keyService.getUserPublicForExchange("brendan");


            System.out.println("**************");
            System.out.println("User's Retrieved Key for Exhange:");
            System.out.println(userKeyForExchange);
            System.out.println("**************");
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
        } else {
            System.out.println("**************");
            System.out.println("NFC ADAPTER NOT NULL");
            System.out.println("**************");
        }

        nfcAdapter.setNdefPushMessageCallback(this, this );

        textView = findViewById(R.id.textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent(), partnerPublicKeyString);

            System.out.println("**************");
            System.out.println("NDEF ACTION DISCOVERED");
            System.out.println("**************");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent= new Intent(this, KeyService.class);
        if (bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            System.out.println("**************");
            System.out.println("SERVICE BOUND");
            System.out.println("**************");
        }

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

        System.out.println("**************");
        System.out.println(ndefRecord.getPayload().toString());
        System.out.println("**************");

        return new NdefMessage(ndefRecord);
    }

    private void processIntent(Intent intent, String partnerName) {
        Parcelable[] rawMessages = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMessages[0];
        partnerPublicKeyString = new String(msg.getRecords()[0].getPayload());
        if (keyService.storePublicKey(partnerName, partnerPublicKeyString)) {
            Toast.makeText(this, "Key Successfully stored", Toast.LENGTH_SHORT).show();
            textView.setText(partnerPublicKeyString);
        }
    }
}