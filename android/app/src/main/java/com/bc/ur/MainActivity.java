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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_SETTINGS_REQUEST = 100;
    TextView transmit, receive;
    EditText fileLocation;
    String[] parts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestCameraPermission();

        transmit = findViewById(R.id.transmit);
        receive = findViewById(R.id.receive);
        fileLocation = findViewById(R.id.file_text);

        transmit.setOnClickListener(this);
        receive.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.transmit) {
            if (!fileLocation.getText().toString().trim().equals("")) {
                byte[] bytes = new byte[0];
                try {
                    bytes = getImageBytes(fileLocation.getText().toString());
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,"Please check your image url and try again",Toast.LENGTH_SHORT)
                                    .show();
                    e.printStackTrace();
                }
                UR ur = UR.create(bytes);

// try-with-resource to make sure encoder is closed as expectation to avoid memory leak
                try (UREncoder encoder = new UREncoder(ur, 30)) {
                    int segNum = (int) encoder.getSeqNum();
                     parts = new String[segNum];
                    for (int i = 0; i < segNum; i++) {
                        parts[i] = encoder.nextPart();
                    }
                    Intent intent = new Intent(MainActivity.this, QRCodes.class);
                    intent.putExtra("parts",parts);
                    startActivity(intent);

                    Log.d(TAG, "onClick: checking parts");
                } catch (URException e) {
                    // error goes here
                } catch (Exception e) {


                }
            } else {
                Toast.makeText(MainActivity.this, "Please input url", Toast.LENGTH_SHORT).show();
            }
        }


        else if (id == R.id.receive) {

            Intent receiveIntent = new Intent(MainActivity.this, Scanner.class);
            startActivity(receiveIntent);
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
}