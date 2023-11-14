package com.capstone.ricedoc;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SheathBlight extends AppCompatActivity {
    TextView diseasename, confidencelevel;
    ImageView imagedisease;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheath_blight);

        String result = getIntent().getStringExtra("text");
        diseasename = findViewById(R.id.diseasename);
        diseasename.setText(result);

        byte[] byteArray = getIntent().getByteArrayExtra("imageByteArray");
        Bitmap receivedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imagedisease = findViewById(R.id.imagedisease);
        imagedisease.setImageBitmap(receivedBitmap);

        String conPercentage = getIntent().getStringExtra("confident_key");
        confidencelevel = findViewById(R.id.confidencelevel);
        confidencelevel.setText(conPercentage);
    }
    @Override
    public void onBackPressed(){
        diseasename.setText("");
        confidencelevel.setText("");
        super.onBackPressed();
    }
}