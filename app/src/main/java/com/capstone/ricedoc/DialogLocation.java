package com.capstone.ricedoc;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogLocation extends DialogFragment {
    private final Set<String> barangaySet = new HashSet<>();
    AlertDialog dialog1, dialog2;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

        // Get the barangay list in the firestore
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("rice_production_data");

        dialog1 = builder1.create();

        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String barangay = document.getString("Barangay");
                            barangaySet.add(barangay);
                        }


                        if (!barangaySet.isEmpty()) {
                            showSecondDialog();
                        } else {
                            List<String> staticBarangays = Arrays.asList("Consolacion", "Dalisay", "Datu Abdul",
                                    "Kuswagan", "Little Panay", "Malativas", "Malitbog", "Manay", "Nanyo",
                                    "Pilar, Southern Davao", "Quezon", "San Roque", "Southern Davao");
                            barangaySet.addAll(staticBarangays);
                            showSecondDialog();
                            Toast.makeText(requireContext(), "No internet connection. Using offline barangay list", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.err.println("ERROR: " + e);
                    }
                });

        return dialog1;
    }

    private void showSecondDialog() {
        if (isAdded()) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
            String[] barangays = barangaySet.toArray(new String[0]);
            List<String> sortedBarangays = new ArrayList<>(Arrays.asList(barangays));
            Collections.sort(sortedBarangays);

            builder2.setTitle("Choose Barangay");
            builder2.setItems(barangays, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (isAdded()) {
                        SharedPreferences preferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("selected_location", barangays[i]);
                        editor.apply();
                        Toast.makeText(getActivity(), "Selected Location: " + barangays[i], Toast.LENGTH_LONG).show();
                        dialog1.dismiss();
                    }
                }
            });

            builder2.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    dialog1.dismiss();
                }
            });
            dialog2 = builder2.create();
            dialog2.show();
        }
    }
}

