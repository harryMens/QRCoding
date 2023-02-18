package com.bc.ur;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import eu.livotov.labs.android.camview.ScannerLiveView;

public class Scanner extends AppCompatActivity {
    private ScannerLiveView camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        camera = findViewById(R.id.camera);
        setCamera();
    }
    void getCodes(){
        List<String> list = new ArrayList<>();
        byte[] bytes = byteString(list);
        UR ur = UR.create(bytes);

// try-with-resource to make sure encoder is closed as expectation to avoid memory leak
        try (UREncoder encoder = new UREncoder(ur, 30);
             URDecoder decoder = new URDecoder()) {
            do {
                String part = encoder.nextPart();
                decoder.receivePart(part);
            } while (!decoder.isComplete());

            // received UR after decoding
            UR resultUR = decoder.resultUR();
           // resultUR.
        } catch (URException e) {
            // error goes here
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                camera.setPlaySound(true);
            }
        });
    }
}