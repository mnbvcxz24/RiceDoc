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
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("recent_scans");

        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Set<String> barangaySet = new HashSet<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String barangay = document.getString("Barangay");
                            barangaySet.add(barangay);

                            // Create card view or handle UI based on the barangay
                            createCardView(linearLayout, barangay);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching recent scans", e);
                        // Handle the failure, e.g., show an error message
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
            System.out.println(selectedLocation);
        }
    }
    private void createCardView(LinearLayout linearLayout, String barangay) {
        if (isAdded()){
            CardView cardView = new CardView(requireContext());
            CardView.LayoutParams layoutParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(15, 16, 15, 16);
            cardView.setLayoutParams(layoutParams);

            TextView textView = new TextView(requireContext());
            textView.setText(barangay);
            textView.setTextSize(18);
            textView.setPadding(15, 16, 15, 16);

            cardView.addView(textView);

            linearLayout.addView(cardView);
        }
    }
}