package com.capstone.ricedoc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HistoryFragment extends Fragment {

    ImageButton btnLocation;
    LinearLayout linearLayout;
    TextView currentLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        currentLocation  = view.findViewById(R.id.currentLocation);
        loadSelectedLocation();

        linearLayout = view.findViewById(R.id.linearLayout);


        // load deviceId
        SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        String deviceId = preferences.getString("deviceId", "");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("recent_scans");

        collectionReference.whereEqualTo("UniqueDeviceID", deviceId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Set<String> barangaySet = new HashSet<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String barangay = document.getString("Barangay");
                            String confidence = document.getString("Confidence");
                            String result = document.getString("Result");
                            barangaySet.add(barangay);

                            // Create card view
                            createCardView(linearLayout, barangay, confidence, result);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching recent scans", e);
                    }
                });

        btnLocation = view.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogLocation().show(getChildFragmentManager(), "dialogLocation");
            }
        });

        return view;
    }

    private void loadSelectedLocation(){
        if (isAdded()) {
            SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

            String selectedLocation = preferences.getString("selected_location", "Please Choose Location");
            currentLocation.setText(selectedLocation);
        }
    }
    private void createCardView(LinearLayout linearLayout, String barangay, String confidence, String result) {
        if (isAdded()){
            CardView cardView = new CardView(requireContext());
            CardView.LayoutParams layoutParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(15, 16, 15, 16);
            cardView.setLayoutParams(layoutParams);

            LinearLayout linearLayoutInsideCard = new LinearLayout(requireContext());
            linearLayoutInsideCard.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            linearLayoutInsideCard.setOrientation(LinearLayout.VERTICAL);

            TextView textView1 = new TextView(requireContext());
            textView1.setText("Barangay: " + barangay);
            textView1.setTextSize(18);
            textView1.setPadding(15, 16, 15, 16);

            TextView textView2 = new TextView(requireContext());
            textView2.setText("Confidence: " + confidence);
            textView2.setTextSize(18);
            textView2.setPadding(15, 16, 15, 16);

            TextView textView3 = new TextView(requireContext());
            textView3.setText("Result: " + result);
            textView3.setTextSize(18);
            textView3.setPadding(15, 16, 15, 16);

            linearLayoutInsideCard.addView(textView1);
            linearLayoutInsideCard.addView(textView2);
            linearLayoutInsideCard.addView(textView3);

            cardView.addView(linearLayoutInsideCard);
            linearLayout.addView(cardView);
        }
    }
}