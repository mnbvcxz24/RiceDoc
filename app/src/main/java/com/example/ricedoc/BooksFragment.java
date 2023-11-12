package com.example.ricedoc;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BooksFragment extends Fragment {

    private CardView Card1, Card2, Card3, Card4, Card5, Card6, Card7;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        Card1 = (CardView) view.findViewById(R.id.card1);

        return view;
    }
}