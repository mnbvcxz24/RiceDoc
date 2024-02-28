package com.capstone.ricedoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class Tungro extends AppCompatActivity {
    TextView diseasename, confidencelevel;
    ImageView imagedisease;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tungro);
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

        // LOAD DISEASE DATA FROM FIREBASE
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("diseases_data");

        collectionReference.whereEqualTo("Language", currentLang())
                .whereEqualTo("DiseaseName", "Tungro")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            TextView content1 = findViewById(R.id.content1);
                            TextView content2 = findViewById(R.id.content2);
                            TextView content3 = findViewById(R.id.content3);

                            String diseaseInfo = document.getString("DiseaseInfo");
                            String disclaimer = document.getString("Disclaimer");

                            List<Object> treatmentInfoList = (List<Object>) document.get("TreatmentInfo");

                            if (treatmentInfoList != null) {
                                String[] treatmentInfo = treatmentInfoList.toArray(new String[treatmentInfoList.size()]);
                                StringBuilder htmlStringBuilder = new StringBuilder();

                                for (int i = 0; i < treatmentInfo.length; i++) {
                                    htmlStringBuilder.append("<b>").append("•").append(". </b>").append(treatmentInfo[i]).append("<br/><br/>");
                                }
                                content2.setText(Html.fromHtml(htmlStringBuilder.toString()));
                            } else {
                                Log.e("Error", "TreatmentInfo is null");
                            }

                            content1.setText(diseaseInfo);
                            content3.setText(disclaimer);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching recent scans", e);
                    }
                });

    }
    private String currentLang(){
        SharedPreferences preferences = this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        String currentLanguage = preferences.getString("selected_language", "en");

        return currentLanguage;
    }
    @Override
    public void onBackPressed(){
        diseasename.setText("");
        confidencelevel.setText("");
        super.onBackPressed();
    }
}