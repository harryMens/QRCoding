package com.bc.ur;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class ImageActivity extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        textView = findViewById(R.id.text_sha_image);
        textView.setMovementMethod(new ScrollingMovementMethod());

        byte[] data = getIntent().getByteArrayExtra("out");

        UR ur23 = UR.create(data);
        String encoded23 = UREncoder.encode(ur23);
        textView.setText(testHashingSha256(encoded23)+"\n "+encoded23);
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

}