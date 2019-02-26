package edu.temple.nfccomponent;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyService extends Service {
    final String ALGORITHM = "RSA";
    final String USER_KEYPAIR_DIR = "USER_KEYPAIR_DIR";
    final String header = "-----BEGIN PUBLIC KEY-----";
    final String footer = "-----END PUBLIC KEY-----";

    IBinder iBinder = new TestBinder();
    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;

    public KeyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class TestBinder extends Binder {
        KeyService getService() {
            return KeyService.this;
        }
    }

    //retrieves and returns a user's keypair from internal storage
    public KeyPair getMyKeyPair() {
        KeyPair myKeyPair = null;
        File userStoreDir = new File(getFilesDir(), USER_KEYPAIR_DIR);
        File userPublicKey = new File(userStoreDir, "userPublicKey");
        File userPrivateKey = new File(userStoreDir, "userPrivateKey");

        byte [] publicKeyBytes;
        byte [] privateKeyBytes;

        //if keypair exists, retrieve it from file
        if (userStoreDir.exists()) {
            if (userPublicKey.exists() && userPrivateKey.exists()) {
                myKeyPair = fetchExistingKeyPair();
            }
            //generate keypair and store it in file
        } else {
            System.out.println("***********PAIR NOT GENERATED******");
            try {
                userStoreDir.mkdir();

                KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
                myKeyPair = kpg.generateKeyPair();

                publicKeyBytes = myKeyPair.getPublic().getEncoded();
                privateKeyBytes = myKeyPair.getPrivate().getEncoded();

                outputStream = new FileOutputStream(userPublicKey);
                outputStream.write(publicKeyBytes);
                outputStream.close();
                outputStream = new FileOutputStream(userPrivateKey);
                outputStream.write(privateKeyBytes);
                outputStream.close();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return myKeyPair;
    }

    /*stores publicKey with partnerName as filename in internal storage
    *NOTE: publicKey is in PEM format*/
    public void storePublicKey(String partnerName, String publicKey) {
        byte [] publicKeyBytes = publicKey.getBytes();

        File keyFile = new File(getFilesDir(), partnerName);
        try {
            outputStream = new FileOutputStream(keyFile);
            outputStream.write(publicKeyBytes);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //retrieves public key saved with partnerName as filename from internal storage
    public RSAPublicKey getPublicKey(String partnerName) {
        File file = new File(getFilesDir(), partnerName);
        KeyFactory factory = null;
        X509EncodedKeySpec spec = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] key = null;
            inputStream.read(key);
            inputStream.close();
            spec = new X509EncodedKeySpec(key);
            factory = KeyFactory.getInstance(ALGORITHM);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    //get user's public key and generate a String representing it in PEM format
    public String generatePEM() {
        KeyPair userKeyPair = fetchExistingKeyPair();
        String PEMString = null;

        if (userKeyPair != null) {
            PublicKey userPublic = userKeyPair.getPublic();

            String mod = userPublic.toString().substring(28, 540);
            StringBuilder sb = new StringBuilder();
            sb.append(header);
            sb.append(mod);
            sb.append(footer);
            PEMString = sb.toString();
        }
        System.out.println(PEMString);
        return PEMString;
    }

    private KeyPair fetchExistingKeyPair() {
        KeyPair myKeyPair = null;
        File userStoreDir = new File(getFilesDir(), USER_KEYPAIR_DIR);
        File userPublicKey = new File(userStoreDir, "userPublicKey");
        File userPrivateKey = new File(userStoreDir, "userPrivateKey");

        byte [] publicKeyBytes;
        byte [] privateKeyBytes;

        try {
            inputStream = new FileInputStream(userPublicKey);
            publicKeyBytes = new byte[(int) userPublicKey.length()];
            inputStream.read(publicKeyBytes);
            inputStream.close();
            inputStream = new FileInputStream(userPrivateKey);
            privateKeyBytes = new byte[(int) userPrivateKey.length()];
            inputStream.read(privateKeyBytes);
            inputStream.close();

            PublicKey publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            myKeyPair = new KeyPair(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return myKeyPair;
    }
}
