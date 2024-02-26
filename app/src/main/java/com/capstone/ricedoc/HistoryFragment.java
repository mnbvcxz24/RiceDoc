package com.capstone.ricedoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.*;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HistoryFragment extends Fragment {

    ImageButton btnLocation, btnInfo;
    LinearLayout linearLayout;
    TextView currentLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // CHECK NETWORK CONNECTIVITY
        if (isNetworkAvailable()) {

        } else {
            Toast.makeText(requireContext(), "You are currently OFFLINE", Toast.LENGTH_SHORT).show();
        }

        // LOAD THE CURRENT LOCATION SELECTED
        currentLocation  = view.findViewById(R.id.currentLocation);
        loadSelectedLocation();

        linearLayout = view.findViewById(R.id.linearLayout);

        // DISPLAY LOADING IF RECENT SCANS IS LOADING
        ProgressDialog loadingDialog = new ProgressDialog(requireContext());
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // LOAD DEVICE ID
        SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        String deviceId = preferences.getString("deviceId", "");

        // LOAD RECENT SCANS DATA FROM FIREBASE
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("recent_scans");

        collectionReference.whereEqualTo("UniqueDeviceID", deviceId)
                .whereEqualTo("Barangay", loadSelectedLocation())
                .orderBy("Date", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // DISMISS LOADING DIALOG WHEN SUCCESS
                        loadingDialog.dismiss();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String barangay = document.getString("Barangay");
                            String confidence = document.getString("Confidence");
                            String result = document.getString("Result");
                            Timestamp timestamp = document.getTimestamp("Date");
                            String imageFileName = document.getString("ImageFileName");
                            String dateScan;

                            if (timestamp != null){
                                Date date = timestamp.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                dateScan = sdf.format(date);
                            } else {
                                dateScan = "Date and Time is not available";
                            }
                            // Create card view
                            createCardView(linearLayout, barangay, confidence, result, dateScan, imageFileName);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // DISMISS THE LOADING DIALOG IF ERROR
                        loadingDialog.dismiss();
                        Toast.makeText(requireContext(), "No Data Available", Toast.LENGTH_LONG).show();
                        Log.e("Firestore", "Error fetching recent scans", e);
                    }
                });

        btnInfo = view.findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogDeviceId().show(getChildFragmentManager(), "dialogDeviceId");
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
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private String loadSelectedLocation(){
        if (isAdded()) {
            SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

            String selectedLocation = preferences.getString("selected_location", "Please Choose Location");
            currentLocation.setText(selectedLocation);

            return selectedLocation;
        }
        return null;
    }
    private void createCardView(LinearLayout linearLayout, String barangay, String confidence, String result, String dateScan, String imageFileName) {
        if (isAdded()){
            CardView cardView = new CardView(requireContext());
            CardView.LayoutParams cardLayoutParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            cardLayoutParams.setMargins(10, 5, 10, 20);
            cardView.setLayoutParams(cardLayoutParams);

            LinearLayout linearLayoutInsideCard = new LinearLayout(requireContext());
            LinearLayout.LayoutParams layoutParamsInsideCard = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParamsInsideCard.setMargins(20, 0, 20, 20);
            linearLayoutInsideCard.setOrientation(LinearLayout.VERTICAL);
            linearLayoutInsideCard.setGravity(Gravity.CENTER);

            // IMAGE VIEW
            String imagePath = "/storage/emulated/0/Pictures/RiceDoc/"+imageFileName;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            ImageView imageIv = new ImageView(requireContext());
            imageIv.setImageBitmap(bitmap);
            imageIv.setLayoutParams(new LinearLayout.LayoutParams(
                    500,
                    500
            ));
            imageIv.setPadding(30, 30, 20, 30);

            // LOCATION TEXT VIEW
            TextView locationTv = new TextView(requireContext());
            String locationCaption = "Barangay: ";
            CharSequence formattedTextLocation = boldText(locationCaption, barangay);
            locationTv.setText(formattedTextLocation, TextView.BufferType.SPANNABLE);
            locationTv.setTextSize(18);
            locationTv.setPadding(30, 10, 20, 30);

            // CONFIDENCE TEXT VIEW
            TextView confidenceTv = new TextView(requireContext());
            String confidenceCaption = "Confidence: ";
            CharSequence formattedTextConfidence = boldText(confidenceCaption, confidence);
            confidenceTv.setText(formattedTextConfidence, TextView.BufferType.SPANNABLE);
            confidenceTv.setTextSize(18);
            confidenceTv.setPadding(30, 10, 20, 30);

            // RESULT TEXT VIEW
            TextView resultTv = new TextView(requireContext());
            String resultCaption = "Result: ";
            CharSequence formattedTextResult = boldText(resultCaption, result);
            resultTv.setText(formattedTextResult, TextView.BufferType.SPANNABLE);
            resultTv.setTextSize(18);
            resultTv.setPadding(30, 10, 20, 30);

            // DATE TEXT VIEW
            TextView dateTv = new TextView(requireContext());
            String dateCaption = "Date Capture: ";
            CharSequence formattedTextDate = boldText(dateCaption, dateScan);
            dateTv.setText(formattedTextDate, TextView.BufferType.SPANNABLE);
            dateTv.setTextSize(18);
            dateTv.setPadding(30, 10, 20, 30);

            linearLayoutInsideCard.addView(imageIv);
            linearLayoutInsideCard.addView(locationTv);
            linearLayoutInsideCard.addView(confidenceTv);
            linearLayoutInsideCard.addView(resultTv);
            linearLayoutInsideCard.addView(dateTv);

            cardView.addView(linearLayoutInsideCard);
            linearLayout.addView(cardView);
        }
    }

    private Spannable boldText(String caption, String dateScan) {
        String fulltext = caption + dateScan;
        SpannableString spannableString = new SpannableString(fulltext);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), caption.length(), fulltext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}
