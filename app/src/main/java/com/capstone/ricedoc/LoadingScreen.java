package com.capstone.ricedoc;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class LoadingScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        Intent loadingIntent = getIntent();
        if (loadingIntent != null) {
            byte[] byteArray = loadingIntent.getByteArrayExtra("imageByteArray");
            String result = loadingIntent.getStringExtra("text");
            String conPercentage = loadingIntent.getStringExtra("confident_key");
            String cleanedConPercentage = conPercentage.replace("%", "");

            new Handler().postDelayed(() -> {
                Intent finalIntent;

                float confidenceValue = Float.parseFloat(cleanedConPercentage);

                if (confidenceValue < 80.0) {
                    Toast.makeText(LoadingScreen.this, "The image is blur or unclear. Please try again.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    return;
                }

                if ("Brown Spot".equals(result)) {
                    finalIntent = new Intent(LoadingScreen.this, description_leafblast.class);
                } else if ("Healthy".equals(result)) {
                    finalIntent = new Intent(LoadingScreen.this, description_leafblast.class);
                } else if ("Leaf Blast".equals(result)) {
                    finalIntent = new Intent(LoadingScreen.this, description_leafblast.class);
                } else if ("Sheath Blight".equals(result)) {
                    finalIntent = new Intent(LoadingScreen.this, description_leafblast.class);
                } else if ("Tungro Virus".equals(result)) {
                    finalIntent = new Intent(LoadingScreen.this, description_leafblast.class);
                } else {
                    Toast.makeText(LoadingScreen.this, "The image is blur or unclear. Please try again.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    return;
                }

                finalIntent.putExtra("imageByteArray", byteArray);
                finalIntent.putExtra("text", result);
                finalIntent.putExtra("confident_key", conPercentage);

                startActivity(finalIntent);

                finish();
            }, 3500);
        }
    }
}