package com.capstone.ricedoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    PredictionFragment predictionFragment = new PredictionFragment();
    BooksFragment booksFragment = new BooksFragment();
    HistoryFragment historyFragment = new HistoryFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.home);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();

        //Enable FireStore Offline support
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        // Generate userId
        SharedPreferences preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        // check if deviceId in sharedpreference is empty
        if(TextUtils.isEmpty(userId)){
            String uniqueID1 = UUID.randomUUID().toString();
            String uniqueID2 = UUID.randomUUID().toString();
            String uniqueUserId = uniqueID1 + "_" + uniqueID2;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("userId", uniqueUserId);
            editor.apply();
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                    return true;
                } else if (itemId == R.id.prediction) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, predictionFragment).commit();
                    return true;
                } else if (itemId == R.id.books) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, booksFragment).commit();
                    return true;
                } else if (itemId == R.id.history) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, historyFragment).commit();
                    return true;
                }
                return false;
            }
        });

    }
}