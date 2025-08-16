package com.example.translator_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText editInput;
    private Button btnTranslate;
    private ImageButton btnSwap;
    private String[] langCodes;
    private OkHttpClient http;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Match IDs from XML
        spinnerFrom = findViewById(R.id.spinnerSource);
        spinnerTo   = findViewById(R.id.spinnerTarget);
        editInput   = findViewById(R.id.etInput);
        btnTranslate= findViewById(R.id.btnTranslate);
        btnSwap     = findViewById(R.id.btnSwap);

        // Set up adapters
        ArrayAdapter<CharSequence> namesAdapter = ArrayAdapter.createFromResource(
                this, R.array.language_names, R.layout.spinner_item);

        namesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerFrom.setAdapter(namesAdapter);
        spinnerTo.setAdapter(namesAdapter);

        // codes array (same order as names)
        langCodes = getResources().getStringArray(R.array.language_codes);

        // Defaults: Auto → English
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);

        http = new OkHttpClient();

        btnSwap.setOnClickListener(v -> {
            int fromPos = spinnerFrom.getSelectedItemPosition();
            int toPos   = spinnerTo.getSelectedItemPosition();
            spinnerFrom.setSelection(toPos);
            spinnerTo.setSelection(fromPos);
        });

        btnTranslate.setOnClickListener(v -> translate());
    }

    private void translate() {
        String input = editInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "Enter text to translate", Toast.LENGTH_SHORT).show();
            return;
        }

        String sl = langCodes[spinnerFrom.getSelectedItemPosition()];
        String tl = langCodes[spinnerTo.getSelectedItemPosition()];

        try {
            String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://translate.googleapis.com/translate_a/single?client=gtx"
                    + "&sl=" + sl + "&tl=" + tl + "&dt=t&q=" + encoded;

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build();

            http.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String msg = "HTTP " + response.code();
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Failed: " + msg, Toast.LENGTH_SHORT).show());
                        return;
                    }

                    String body = response.body().string();
                    String translated = parseTranslation(body);

                    runOnUiThread(() -> {
                        if (translated == null) {
                            Toast.makeText(MainActivity.this, "Parse error", Toast.LENGTH_SHORT).show();
                        } else {
                            // ✅ Open ResultActivity instead of showing in TextView
                            Intent resultIntent = new Intent(MainActivity.this, ResultActivity.class);
                            resultIntent.putExtra("fromLang", spinnerFrom.getSelectedItem().toString());
                            resultIntent.putExtra("toLang", spinnerTo.getSelectedItem().toString());
                            resultIntent.putExtra("originalText", input);
                            resultIntent.putExtra("translatedText", translated);
                            startActivity(resultIntent);
                        }
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Response format is a nested JSON array. We need the first array's items' first element.
     * Example: [[["translated","original",...], ...], ...]
     */
    private String parseTranslation(String json) {
        try {
            JSONArray root = new JSONArray(json);
            JSONArray sentences = root.getJSONArray(0);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sentences.length(); i++) {
                JSONArray seg = sentences.getJSONArray(i);
                sb.append(seg.getString(0)); // seg[0] is translated text
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
