package com.capstone.ricedoc;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoadingScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        Intent loadingIntent = getIntent();
        if (loadingIntent != null) {
            byte[] byteArray = loadingIntent.getByteArrayExtra("imageByteArray");
            String result = loadingIntent.getStringExtra("disease");
            String conPercentage = loadingIntent.getStringExtra("confident_key");
            String cleanedConPercentage = conPercentage.replace("%", "");

            // Get the current date and time
            Date currentTime = Calendar.getInstance().getTime();
            Timestamp dateTime = new Timestamp(currentTime);

            // load deviceId
            SharedPreferences preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
            String deviceId = preferences.getString("deviceId", "");

            // Load the selectedLocation in sharedpreference
            String selectedLocation = preferences.getString("selected_location", "");

            if(TextUtils.isEmpty(selectedLocation)) {
                Toast.makeText(LoadingScreen.this, "Please Select Location First", Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {

                new Handler().postDelayed(() -> {
                    Intent finalIntent;

                    float confidenceValue = Float.parseFloat(cleanedConPercentage);

                    if (confidenceValue < 80.0) {
                        Toast.makeText(LoadingScreen.this, "The image is blur or unclear. Please try again.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("UniqueDeviceID", deviceId);
                    data.put("Barangay", selectedLocation);
                    data.put("Result", result);
                    data.put("Confidence", cleanedConPercentage+"%");
                    data.put("Date", dateTime);

                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = firebaseFirestore.collection("recent_scans");
                    collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Firestore", "Error adding document", e);
                        }
                    });


                    if ("Brown Spot".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, BrownSpot.class);
                    } else if ("Healthy".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, Healthy.class);
                    } else if ("Leaf Blast".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, LeafBlast.class);
                    } else if ("Leaf Folder".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, LeafFolder.class);
                    } else if ("Sheath Blight".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, SheathBlight.class);
                    } else if ("Stem Borer".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, StemBorer.class);
                    } else if ("Tungro Virus".equals(result)) {
                        finalIntent = new Intent(LoadingScreen.this, Tungro.class);
                    } else if ("Unknown".equals(result)) {
                        Toast.makeText(LoadingScreen.this, "Please make sure that only the rice leaf is captured. Please try again.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return;
                    } else {
                        Toast.makeText(LoadingScreen.this, "The image is blur or unclear. Please try again.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return;
                    }

                    finalIntent.putExtra("imageByteArray", byteArray);
                    finalIntent.putExtra("text", result);
                    finalIntent.putExtra("confident_key", conPercentage);

                    startActivity(finalIntent);

                    finish();
                }, 2500);
            }
        }
    }
}