package com.example.ricedoc;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BooksFragment extends Fragment implements View.OnClickListener {

    private CardView card1, card2, card3, card4, card5, card6, card7;

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
}

