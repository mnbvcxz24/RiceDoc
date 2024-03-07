package com.capstone.ricedoc;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogUserId extends DialogFragment {
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (isAdded()) {
            SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
            String userId = preferences.getString("userId", "No User ID created");

            builder.setTitle("User ID")
                    .setMessage("Your user ID is: " + userId)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        // Create and return the AlertDialog
        return builder.create();
    }
}
