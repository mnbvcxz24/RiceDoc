package com.capstone.ricedoc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;
import java.util.Set;

public class DialogLocation extends DialogFragment {
    private Set<String> barangaySet = new HashSet<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

        // Get the barangay list in the firestore
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("rice_production_data");

        AlertDialog dialog1 = builder1.create();

        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String barangay = document.getString("Barangay");
                            barangaySet.add(barangay);
                        }
                        showSecondDialog();
                        dialog1.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.err.println("ERROR: " + e);
                    }
                });

        // Return null to avoid creating an empty AlertDialog here
        return dialog1;
    }

    private void showSecondDialog() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
        String[] barangays = barangaySet.toArray(new String[0]);

        builder2.setTitle("Choose Barangay");
        builder2.setItems(barangays, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog2, int i) {
                System.out.println("Barangay : " + barangays[i]);
                //Toast.makeText(getActivity(), "Barangay : " + barangays[i], Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }
}


