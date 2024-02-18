package com.capstone.ricedoc;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HistoryFragment extends Fragment {

    ImageButton btnLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recent_scan");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> barangaySet = new HashSet<>();

                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    String barangay = entrySnapshot.child("barangay").getValue(String.class);
                    barangaySet.add(barangay);
                }

                System.out.println("Unique Barangay :"+barangaySet);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Error "+error.getMessage());
            }
        });

        ArrayList<String> barangayList = new ArrayList<>();

        btnLocation = view.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogLocation().show(getChildFragmentManager(), "dialogLocation");
            }
        });
        return view;
    }
}