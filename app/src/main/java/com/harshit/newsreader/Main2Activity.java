package com.harshit.newsreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Main2Activity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().getDisplayZoomControls();
        webView.setWebViewClient(new WebViewClient());
        //webView.loadData(intent.getStringExtra("url"), "text/html", "UTF-8");
        webView.loadUrl(intent.getStringExtra("url"));
    }
}
