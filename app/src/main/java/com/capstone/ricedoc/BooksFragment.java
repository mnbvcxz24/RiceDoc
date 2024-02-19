package com.capstone.ricedoc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toolbar;

import com.capstone.ricedoc.R;

import java.util.Locale;

public class BooksFragment extends Fragment implements View.OnClickListener {

    private CardView card1, card2, card3, card4, card5, card6, card7;
    ImageButton btnLanguage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        card1 = view.findViewById(R.id.card1);
        card1.setOnClickListener(this);

        card2 = view.findViewById(R.id.card2);
        card2.setOnClickListener(this);

        card3 = view.findViewById(R.id.card3);
        card3.setOnClickListener(this);

        card4 = view.findViewById(R.id.card4);
        card4.setOnClickListener(this);

        card5 = view.findViewById(R.id.card5);
        card5.setOnClickListener(this);

        card6 = view.findViewById(R.id.card6);
        card6.setOnClickListener(this);

        card7 = view.findViewById(R.id.card7);
        card7.setOnClickListener(this);

        //CLICK LISTENER FOR THE LANGUAGE BUTTON
        btnLanguage = view.findViewById(R.id.btnLanguage);
        btnLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageMenu(v);
            }
        });

        setLocale(loadSelectedLanguage());
        return view;
    }
    @Override
    public void onClick(View v) {
        Intent i;

        int id = v.getId();
        if (id == R.id.card1) {
            i = new Intent(getActivity(), BookCard1.class);
            startActivity(i);
        } else if (id == R.id.card2) {
            i = new Intent(getActivity(), BookCard2.class);
            startActivity(i);
        } else if (id == R.id.card3) {
            i = new Intent(getActivity(), BookCard3.class);
            startActivity(i);
        } else if (id == R.id.card4) {
            i = new Intent(getActivity(), BookCard4.class);
            startActivity(i);
        } else if (id == R.id.card5) {
            i = new Intent(getActivity(), BookCard5.class);
            startActivity(i);
        } else if (id == R.id.card6) {
            i = new Intent(getActivity(), BookCard6.class);
            startActivity(i);
        } else if (id == R.id.card7) {
            i = new Intent(getActivity(), BookCard7.class);
            startActivity(i);
        }
    }
    private void showLanguageMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.inflate(R.menu.language_menu);

        // Set item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_item_english) {
                    setLocale("en");
                    return true;
                } else if (itemId == R.id.menu_item_cebuano) {
                    setLocale("ceb");
                    return true;
                }
                return false;
            }
        });
        // Show the popup menu
        popupMenu.show();
    }
    private void setLocale(String languageCode) {
        Locale newLocale = new Locale(languageCode);
        Locale currentLocale = getResources().getConfiguration().locale;

        if (!currentLocale.equals(newLocale)) {
            Locale.setDefault(newLocale);

            Configuration config = new Configuration();
            config.locale = newLocale;

            Context context = requireContext();
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

            saveSelectedLanguage(languageCode);

            restartActivity();
        }
    }
    private void saveSelectedLanguage(String languageCode) {
        SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();
    }
    private String loadSelectedLanguage() {
        SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

        return preferences.getString("selected_language", "en");
    }
    private void restartActivity() {
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        requireActivity().startActivity(intent);
    }
}

