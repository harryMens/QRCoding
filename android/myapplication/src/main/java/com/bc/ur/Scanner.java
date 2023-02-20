package com.bc.ur;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eu.livotov.labs.android.camview.ScannerLiveView;
import eu.livotov.labs.android.camview.scanner.decoder.zxing.ZXDecoder;

public class Scanner extends AppCompatActivity {
    private static final String TAG = "Scanner";

    MyVIewModel vIewModel;

    private ScannerLiveView camera;
    List<String> list = new ArrayList<>();
    HashMap<Integer, byte[]> hashMap = new HashMap<>();
    List<byte[]> byteList = new ArrayList<>();
    List<Integer> integers = new ArrayList<>();
    int size = 0;
    TextView textView;
    byte[] out;
    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        camera = findViewById(R.id.camera);
        textView = findViewById(R.id.text_count);
        vIewModel = new ViewModelProvider(this).get(MyVIewModel.class);
        button = findViewById(R.id.next_pic);

        button.setOnClickListener(v -> {
                addingHashMap();

        });
        if (checkPermission()) {
            // if permission is already granted display a toast message
            Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
        setCamera();
        //setVIewModel();
    }




    byte[] byteString(List<String> stringList){
        StringBuilder buffer = new StringBuilder();
        String line;

        for (String string :stringList){
            buffer.append(string);
        }
        return buffer.toString().getBytes();
    }
    void setCamera(){
        camera.setScannerViewEventListener(new ScannerLiveView.ScannerViewEventListener() {
            @Override
            public void onScannerStarted(ScannerLiveView scanner) {

            }

            @Override
            public void onScannerStopped(ScannerLiveView scanner) {

            }

            @Override
            public void onScannerError(Throwable err) {
            }

            @Override
            public void onCodeScanned(String data) {
                Log.d(TAG, "onCodeScanned: data receiving "+data);
                // Toast.makeText(Scanner.this, "data is "+data, Toast.LENGTH_SHORT).show();
                camera.setPlaySound(true);

                try {
                    UR hashCause = URDecoder.decode(data);
                    if (isItSizeData(data)){
                        int[] intArray = new int[hashCause.getMessage().length];
                        for (int i = 0; i < hashCause.getMessage().length; i++) { intArray[i] = hashCause.getMessage()[i]; }
                        size = intArray[3];
                        Log.d(TAG, "onCodeScanned: checking for size");

                        textView.setText(list.size()+"/"+size);
                    }
                    else if (dataIsNotRetrievedYet(data) && checkingMach(hashCause)){
                       // vIewModel.data.setValue(data);
                        list.add(data);

                        UR ur = URDecoder.decode(data);
                        ByteBuffer bb = ByteBuffer.wrap(ur.getMessage());

                        byte[] message = new byte[ur.getMessage().length-8];
                        byte[] number = new byte[4];
                        byte[] hashCode = new byte[4];

                        bb.get(message, 0, message.length);
                        bb.get(number, 0, number.length);
                        bb.get(hashCode, 0, hashCode.length);

                        //int hashNumber = ByteBuffer.wrap(hashCode).getInt();

                        int hashNumberValue = ByteBuffer.wrap(number).getInt();
                        byteList.add(message);
                        integers.add(hashNumberValue);

                        if (size >0) {
                            textView.setText(list.size() +"/"+ size);
                        }
                    }
                }
                catch (Exception e){
                    Log.e(TAG, "onCodeScanned: there is exception error "+e.getMessage());
                }
            }
        });
    }

    void addingHashMap(){
            Log.d(TAG, "addingHashMap: haspmap "+hashMap.size());
            try {
//                UR ur = URDecoder.decode(data);
//                ByteBuffer bb = ByteBuffer.wrap(ur.getMessage());
//
//                byte[] message = new byte[ur.getMessage().length-8];
//                byte[] number = new byte[4];
//                byte[] hashCode = new byte[4];
//
//                bb.get(message, 0, message.length);
//                bb.get(number, 0, number.length);
//                bb.get(hashCode, 0, hashCode.length);
//
//                //int hashNumber = ByteBuffer.wrap(hashCode).getInt();
//
//                bb.get(message, 0, message.length);
//                bb.get(number, 0, number.length);
//
//                Log.d(TAG, "addingHashMap: before");
//                int hashNumberValue = ByteBuffer.wrap(hashCode).getInt();
//                HashMap<Integer, byte[]> hash = new HashMap<>();
//                hash.put(hashNumberValue,message);
//
//                Log.d(TAG, "addingHashMap: after one");
//                // this will check the hash code and compare
//                ByteBuffer bb1 = ByteBuffer.wrap(ur.getMessage());
//                byte[] ghana = new byte[ur.getMessage().length-4];
//                bb1.get(ghana, 0, ghana.length);
//                Log.d(TAG, "addingHashMap: after 2");
//
//                //hashMap.add(hash);
//                Log.d(TAG, "addingHashMap: after all");
                addingCodes();

            }
            catch (Exception e){
                Log.e(TAG, "isItSizeData: decode exception  error "+e.getMessage() );
            }

    }




    boolean checkingMach(UR ur){
// this will check the hash code and compare
        ByteBuffer bb1 = ByteBuffer.wrap(ur.getMessage());
        byte[] ghana = new byte[ur.getMessage().length-4];
        byte[] hashCode = new byte[4];
        bb1.get(ghana, 0, ghana.length);
        bb1.get(hashCode, 0, hashCode.length);

        int hashNumber = ByteBuffer.wrap(hashCode).getInt();

        UR uring = UR.create(ghana);
        String encodedHash = UREncoder.encode(uring);
        if (encodedHash.hashCode() == hashNumber){
            return true;
        }
        return false;
    }
    public static void bubbleSort(int[] a, byte[] b) {
        boolean sorted = false;
        int temp;
        while(!sorted) {
            sorted = true;
            for (int i = 0; i < a.length - 1; i++) {
                if (a[i] > a[i+1]) {
                    temp = a[i];
                    a[i] = a[i+1];
                    a[i+1] = temp;
                    sorted = false;
                }
            }
        }
    }
    void addingCodes() throws IOException {
        Log.d(TAG, "addingCodes: entered hashmaps " + integers.size());
        Log.d(TAG, "addingCodes: entered bytes " + byteList.size());

        for (int i =0; i< integers.size();i++){
            hashMap.put(integers.get(i),byteList.get(i));
        }

        Collections.sort(integers);

        for (int i =0; i< hashMap.size(); i++){
            if (out != null && out.length > 0) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                output.write(out);
                output.write(hashMap.get(integers.get(i)));

                out = output.toByteArray();
            }
            else{
                out = hashMap.get(integers.get(i));
            }
        }
//        for (int i =0; i< hashMap.size();i++){
//          //  byte [] byting = out;
//            Toast.makeText(this, "is happened", Toast.LENGTH_SHORT).show();
//            if (out != null && out.length > 0) {
//                ByteArrayOutputStream output = new ByteArrayOutputStream();
//
//                output.write(out);
//                output.write(hashMap.get(i).get(i));
//
//                out = output.toByteArray();
//            }
//            else{
//                out = hashMap.get(i).get(i);
//            }
//        }
        Log.d(TAG, "addingCodes: outting "+ Arrays.toString(out));
        Intent intent = new Intent(Scanner.this, ImageActivity.class);
        intent.putExtra("out", out);
        startActivity(intent);
        finish();

    }
    boolean isItSizeData(String data){
        try {
            UR ur = URDecoder.decode(data);
            if (ur.getMessage().length<9){
                return true;
            }
        }
        catch (Exception e){
            Log.e(TAG, "isItSizeData: decode exception  error "+e.getMessage() );
        }
        return false;
    }
    boolean dataIsNotRetrievedYet(String data){
        boolean notReceived = true;
        for (String string : list){
            if (data.trim().equals(string)){
                notReceived = false;
            }
        }
        return notReceived;
    }
    @Override
    protected void onResume() {
        super.onResume();
        ZXDecoder decoder = new ZXDecoder();
        // 0.5 is the area where we have
        // to place red marker for scanning.
        decoder.setScanAreaPercent(0.8);
        // below method will set decoder to camera.
        camera.setDecoder(decoder);
        camera.startScanner();
    }

    @Override
    protected void onPause() {
        // on app pause the
        // camera will stop scanning.
        camera.stopScanner();
        super.onPause();
    }

    private boolean checkPermission() {
        // here we are checking two permission that is vibrate
        // and camera which is granted by user and not.
        // if permission is granted then we are returning
        // true otherwise false.
        int camera_permission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int vibrate_permission = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATE);
        return camera_permission == PackageManager.PERMISSION_GRANTED && vibrate_permission == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {
        // this method is to request
        // the runtime permission.
        int PERMISSION_REQUEST_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, VIBRATE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // this method is called when user
        // allows the permission to use camera.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean vibrateaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (cameraaccepted && vibrateaccepted) {
                Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied \n You cannot use app without providing permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    void setVIewModel(){
        vIewModel.data.observe(this, s -> {
            if (s != null){
                list.add(s);
//                addingHashMap(s);
            }
        });
    }
}