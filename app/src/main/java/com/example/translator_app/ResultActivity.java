package com.example.translator_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends AppCompatActivity {

    TextView tvLanguageInfo, tvOriginal, tvTranslated, tvTranslatedLength;
    ImageButton btnBack, btnShare;
    Button btnCopy, btnNewTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize
        tvLanguageInfo = findViewById(R.id.tvLanguageInfo);
        tvOriginal = findViewById(R.id.tvOriginal);
        tvTranslated = findViewById(R.id.tvTranslated);
        tvTranslatedLength = findViewById(R.id.tvTranslatedLength);

        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnCopy = findViewById(R.id.btnCopy);
        btnNewTranslation = findViewById(R.id.btnNewTranslation);

        // Receive data from MainActivity
        Intent intent = getIntent();
        String fromLang = intent.getStringExtra("fromLang");
        String toLang = intent.getStringExtra("toLang");
        String originalText = intent.getStringExtra("originalText");
        String translatedText = intent.getStringExtra("translatedText");

        // Populate UI
        tvLanguageInfo.setText(fromLang + " → " + toLang);
        tvOriginal.setText(originalText);
        tvTranslated.setText(translatedText);
        tvTranslatedLength.setText(translatedText.length() + " characters");

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Share button
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, translatedText);
            startActivity(Intent.createChooser(shareIntent, "Share Translation"));
        });

        // Copy button
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Translated Text", translatedText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(ResultActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // New translation button → go back to MainActivity
        btnNewTranslation.setOnClickListener(v -> {
            Intent mainIntent = new Intent(ResultActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }
}
