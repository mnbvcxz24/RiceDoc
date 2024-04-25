package com.capstone.ricedoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoadingScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        Intent loadingIntent = getIntent();
        if (loadingIntent != null) {
            byte[] imageByteArray = loadingIntent.getByteArrayExtra("imageByteArray");
            String result = loadingIntent.getStringExtra("disease");
            String conPercentage = loadingIntent.getStringExtra("confident_key");
            String cleanedConPercentage = conPercentage.replace("%", "");

            // Get the current date and time
            Date currentTime = Calendar.getInstance().getTime();
            Timestamp dateTime = new Timestamp(currentTime);

            // load deviceId
            SharedPreferences preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
            String userId = preferences.getString("userId", "");

            // Load the selectedLocation in sharedpreference
            String selectedLocation = preferences.getString("selected_location", "");

            if(TextUtils.isEmpty(selectedLocation)) {
                Toast.makeText(LoadingScreen.this, "Please select a location first.", Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {

                new Handler().postDelayed(() -> {
                    Intent finalIntent;

                    float confidenceValue = Float.parseFloat(cleanedConPercentage);

                    if (confidenceValue < 80.0) {
                        Toast.makeText(LoadingScreen.this, "The image is blurry or unclear. Please try rotating or cropping it.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return;
                    }

                    if ("Brown Spot".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, BrownSpot.class);
                    } else if ("Healthy".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, Healthy.class);
                    } else if ("Leaf Blast".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, LeafBlast.class);
                    } else if ("Leaf Folder".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, LeafFolder.class);
                    } else if ("Sheath Blight".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, SheathBlight.class);
                    } else if ("Stem Borer".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, StemBorer.class);
                    } else if ("Tungro Virus".equals(result)) {
                        saveImage(imageByteArray, dateTime, selectedLocation);
                        uploadData(userId, selectedLocation, result, cleanedConPercentage, dateTime);
                        finalIntent = new Intent(LoadingScreen.this, Tungro.class);
                    } else if ("Unknown".equals(result)) {
                        Toast.makeText(LoadingScreen.this, "Ensure only the rice leaf is captured. Please try again.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return;
                    } else {
                        Toast.makeText(LoadingScreen.this, "The image is blurry or unclear. Please try rotating or cropping it.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                        return;
                    }

                    finalIntent.putExtra("imageByteArray", imageByteArray);
                    finalIntent.putExtra("text", result);
                    finalIntent.putExtra("confident_key", conPercentage);

                    startActivity(finalIntent);

                    finish();
                }, 2500);
            }
        }
    }
    private void uploadData(String userId, String selectedLocation, String result, String cleanedConPercentage, Timestamp dateTime){
        Map<String, Object> data = new HashMap<>();

        Date date = dateTime.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault());
        String dateScan = sdf.format(date);
        String imageFileName = "IMG_" + dateScan + "_" + selectedLocation + ".jpg";

        data.put("UniqueUserID", userId);
        data.put("Barangay", selectedLocation);
        data.put("Result", result);
        data.put("Confidence", cleanedConPercentage + "%");
        data.put("ImageFileName", imageFileName);
        data.put("Date", dateTime);


        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("recent_scans");
        collectionReference.add(data).addOnSuccessListener(documentReference -> Log.d("Firestore", "Document added with ID: " + documentReference.getId())).addOnFailureListener(e -> Log.e("Firestore", "Error adding document", e));
    }
    private void saveImage(byte[] imageByteArray, Timestamp dateTime, String selectedLocation){
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);

        // Create dir folder for image to save
        File riceDocFolder = createImageGalleryFolder();

        // Create unique file name
        Date date = dateTime.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault());
        String dateScan = sdf.format(date);
        String imageFileName = "IMG_" + dateScan + "_" + selectedLocation + ".jpg";

        // Save the image to the file
        File imageFile = new File(riceDocFolder, imageFileName);
        saveBitmapToFile(imageBitmap, imageFile);

    }
    private static File createImageGalleryFolder() {
        // Create the RiceDoc folder
        File riceDocFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ".RiceDoc");

        // Create the folder if it doesn't exist
        if (!riceDocFolder.exists()) {
            if (!riceDocFolder.mkdirs()) {
                Log.e("ImageSaver", "Failed to create directory");
                return null;
            }
        }
        return riceDocFolder;
    }

    private static void saveBitmapToFile(Bitmap imageBitmap, File imageFile) {
        try (OutputStream outputStream = new FileOutputStream(imageFile)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}