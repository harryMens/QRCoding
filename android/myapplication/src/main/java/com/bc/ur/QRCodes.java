package com.bc.ur;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodes extends AppCompatActivity {
    private static final String TAG = "QRCodes";

    ImageView codeImage;
    QRGEncoder qrgEncoder;
    TextView textView;
    boolean doAgain = true;
    int size = 0;
    boolean showed = true;
    List<String> strings;
    String numberData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodes);

        codeImage = findViewById(R.id.code_image);
        textView = findViewById(R.id.text_count_code);
        String test = "Kofi is a boy";
        Log.d(TAG, "onCreate: checking learning hash code "+test.hashCode());
        strings = (List<String>) getIntent().getSerializableExtra("stringList");
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        numberData = strings.get(strings.size()-1);
                        if (size < strings.size()) {
                                showed = true;
                                displayCodeImages(strings.get(size));
                        }
                        else {
                            size = 0;
                            displayCodeImages(strings.get(size));
                        }
                            size++;
                    }
                });
            }
        }, 0, 800);
//        int times = 0;
//        do {
//            if (size >= strings.size()){
//                size = 0;
//                times++;
//            }
//            if (times >3){
//                doAgain = false;
//            }
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    displayCodeImages(strings.get(size));
//                }
//            },1000);
//
//            size++;
//        }
//        while (doAgain);

    }
    private void displayCodeImages(String code){
        // below line is for getting
        // the windowmanager service.
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = Math.min(width, height);
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(code, null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            codeImage.setImageBitmap(bitmap);
            codeImage.invalidate();

            int stringLength = strings.size()-1;
            textView.setText(size+"/"+stringLength);

//            codeImage.setImageDrawable(d);
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }
    }
}