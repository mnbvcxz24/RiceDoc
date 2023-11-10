package com.example.ricedoc;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PredictionFragment extends Fragment {
    private WebView webView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prediction, container, false);

        webView = view.findViewById(R.id.webview);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed

        // Set cache mode to allow using cache if available
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Set a WebViewClient to handle page navigation
        webView.setWebViewClient(new WebViewClient());

        // Load a web page
        webView.loadUrl("https://moldy24.pythonanywhere.com"); // Replace with your

        return view;
    }
}