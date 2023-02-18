package com.bc.ur;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_SETTINGS_REQUEST = 100;
    TextView transmit, receive;
    EditText fileLocation;
    ProgressBar loading;
    ImageView image;
    FloatingActionButton actionButton;
    LinearLayout layout;
    int entireList = 0;
    String entireString;
    TextView shaText;
    String[] parts;
    List<String> stringList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestCameraPermission();

        transmit = findViewById(R.id.transmit);
        receive = findViewById(R.id.receive);
        fileLocation = findViewById(R.id.file_text);
        loading = findViewById(R.id.loading);
        image = findViewById(R.id.image);
        actionButton = findViewById(R.id.action_button);
        layout = findViewById(R.id.action_container);
        shaText = findViewById(R.id.text_sha);
        shaText.setMovementMethod(new ScrollingMovementMethod());

        String test = "Kofi is a boy";
        Log.d(TAG, "onCreate: checking learning hash code "+test.hashCode());

        byte [] numberedBytes =  ByteBuffer.allocate(4).putInt(test.hashCode()).array();
        Log.d(TAG, "onCreate: getting byte "+ Arrays.toString(numberedBytes));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        fileLocation.setText("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png");
        transmit.setOnClickListener(this);
        receive.setOnClickListener(this);
        actionButton.setOnClickListener(this);

    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }
    void requestCameraPermission() {
        ActivityResultLauncher<String[]> someActivityResultLauncher1 =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            Boolean camera = result.getOrDefault(Manifest.permission.CAMERA, false);

                            if (camera != null && camera) {
                                Log.d(TAG, "MainActivity: camera enabled");
                            } else {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, LOCATION_SETTINGS_REQUEST);
                            }
                        }
                    }
                });
        ActivityResultLauncher<Intent> someActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                            }
                        });

        someActivityResultLauncher1.launch(new String[]{
                Manifest.permission.CAMERA
        });
    }
    static String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.transmit) {
            loading.setVisibility(View.VISIBLE);
          //  Toast.makeText(this, "please wait...", Toast.LENGTH_SHORT).show();

            if (!fileLocation.getText().toString().trim().equals("")) {
                stringList.clear();
                byte[] bytes;
                try {

                    bytes = getImageBytes(fileLocation.getText().toString());

                    UR ur23 = UR.create(bytes);
                    String encoded23 = UREncoder.encode(ur23);
                    shaText.setText(testHashingSha256(encoded23)+"\n "+encoded23);

                    List<byte[]> byteList = byteList(bytes);
                    Log.d(TAG, "onClick: checking byteList size "+ byteList.size());
                    Log.d(TAG, "onClick: checking bytes "+ Arrays.toString(bytes));
                    List<UR> urList = new ArrayList<>();
                    for (byte[] byte1 : byteList){
                      //  Log.d(TAG, "onClick: checking byteList");
                        UR ur = UR.create(byte1);
                        String encoded = UREncoder.encode(ur);
                        byte [] numberedBytes =  ByteBuffer.allocate(4).putInt(encoded.hashCode()).array();

                        ByteArrayOutputStream output = new ByteArrayOutputStream();

                        output.write(byte1);
                        output.write(numberedBytes);

                        byte[] out = output.toByteArray();
                        UR ur1 = UR.create(out);
                        urList.add(ur1);

                        Log.d(TAG, "onClick: main array "+ Arrays.toString(ur1.getMessage()));
                        Log.d(TAG, "onClick: hash "+ Arrays.toString(numberedBytes));
                    }

                    for (UR ur : urList){
                        String encoded = UREncoder.encode(ur);
                        stringList.add(encoded);
                        UR urDecode = URDecoder.decode(encoded);
//                        Log.d(TAG, "onClick: encode "+ encoded);
//                        Log.d(TAG, "onClick: encode size "+ encoded.length());

                        byte [] removeNumbers = urDecode.getMessage();
                        byte [] main = new byte[removeNumbers.length-4];
                        byte [] number = new byte[4];
                        ByteBuffer bb = ByteBuffer.wrap(removeNumbers);
                        bb.get(main, 0, main.length);
                        bb.get(number, 0, number.length);


                        byte [] numberedBytes =  ByteBuffer.allocate(4).putInt(encoded.hashCode()).array();
                        Log.d(TAG, "onCreate: getting byte of encode "+ Arrays.toString(numberedBytes));

                        Log.d(TAG, "onClick: string size "+ encoded.hashCode());


                        layout.setVisibility(View.GONE);
                        actionButton.setVisibility(View.VISIBLE);
                        Drawable drawable = LoadImageFromWebOperations(fileLocation.getText().toString());
                        image.setImageDrawable(drawable);
                        image.setVisibility(View.VISIBLE);
                        fileLocation.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        shaText.setVisibility(View.VISIBLE);

//                        Log.d(TAG, "onClick: checking Cbor "+ Arrays.toString(urDecode.getCbor()));
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            else {
                //Toast.makeText(MainActivity.this, "Please input url", Toast.LENGTH_SHORT).show();
            }
        }


        else if (id == R.id.receive) {

            Intent receiveIntent = new Intent(MainActivity.this, Scanner.class);
            startActivity(receiveIntent);
        }
        else if (id == R.id.action_button){
//            stringList.add(encoded);
            Log.d(TAG, "onClick: first list "+stringList.get(0));
            Intent intent = new Intent(MainActivity.this, QRCodes.class);
            intent.putExtra("stringList", (Serializable) stringList);
            startActivity(intent);
        }
    }

    private static byte[] getImageBytes(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (InputStream stream = url.openStream())
        {
            byte[] buffer = new byte[4096];

            while (true)
            {
                int bytesRead = stream.read(buffer);
                if (bytesRead < 0) { break; }
                output.write(buffer, 0, bytesRead);
            }
        }

        return output.toByteArray();
    }
    List<byte[]> byteList(byte[] bytes) throws IOException {
        boolean doAgain = true;
        List<byte[]> byteList = new ArrayList<>();
        int byteIndex = 0;
        byte counting = 1;

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        if (bytes.length >250) {
            do {
                if (bytes.length - byteIndex >250) {
                    byte[] extra = new byte[250];
                    bb.get(extra, 0, extra.length);


                    byte [] numberedBytes =  ByteBuffer.allocate(4).putInt(counting).array();


                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    output.write(extra);
                    output.write(numberedBytes);

                    byte[] out = output.toByteArray();
                    byteList.add(out);
                }
                else{
                    int nam = bytes.length - byteIndex;
                    byte[] extra = new byte[nam];
                    bb.get(extra, 0, extra.length);
                    byte [] numberedBytes =  ByteBuffer.allocate(4).putInt(counting).array();


                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    output.write(extra);
                    output.write(numberedBytes);

                    byte[] out = output.toByteArray();
                    byteList.add(out);
                    doAgain = false;
                }
                byteIndex +=250;
                counting++;
            }
            while (doAgain);
        }
        else {
            return Collections.singletonList(bytes);
        }
        byte [] numberedBytes =  ByteBuffer.allocate(4).putInt(byteList.size()).array();

        byteList.add(numberedBytes);
        return byteList;
    }

    public String testHashingSha256(String data) {
        final HashFunction hashFunction = Hashing.sha256();
        final HashCode hc = hashFunction
                .newHasher()
                .putString(data, Charsets.UTF_8)
                .hash();
        final String sha256 = hc.toString();
        return "sha256 = "+sha256;
    }
    @Override
    public void onBackPressed() {
        if (actionButton.getVisibility() == View.VISIBLE){
            loading.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.GONE);
            shaText.setVisibility(View.GONE);

            image.setVisibility(View.GONE);
            fileLocation.setVisibility(View.VISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }
}