package com.capstone.ricedoc;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.ricedoc.R;

public class description_leafblast extends AppCompatActivity {
    TextView nameDisease;
    TextView confidencelevel;
    ImageView pictureDisease;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_leafblast);

        String result = getIntent().getStringExtra("text");
        nameDisease = findViewById(R.id.textView);
        nameDisease.setText(result);

        byte[] byteArray = getIntent().getByteArrayExtra("imageByteArray");
        Bitmap receivedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        pictureDisease = findViewById(R.id.imageView3);
        pictureDisease.setImageBitmap(receivedBitmap);

        String conPercentage = getIntent().getStringExtra("confident_key");
        confidencelevel = findViewById(R.id.confidencelevel);
        confidencelevel.setText(conPercentage);
    }
    @Override
    public void onBackPressed(){
        nameDisease.setText("");
        confidencelevel.setText("");
        super.onBackPressed();
    }
}