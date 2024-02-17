package com.capstone.ricedoc;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogLocation extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String [] barangays = getActivity().getResources().getStringArray(R.array.barangay);

        builder.setTitle("Choose Barangay");
        builder.setItems(barangays, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(getActivity(), "Barangay : " + barangays[i], Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();
    }
}
