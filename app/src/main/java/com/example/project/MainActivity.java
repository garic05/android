package com.example.project;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String FIND_TAG = "FIND";
    public static final String REC_TAG = "REC";
    public static final String ERROR_TAG = "ERROR";
    public static final String LOG_TAG = "MY_TAG";
    private Button buttonFind, buttonRec;
    private Dialog dialog;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private final String[] langs = new String[]{"ru", "en", "fr", "de"};
    private String lang = langs[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, langs);
        spinner = findViewById(R.id.spinner);

        spinner.setPrompt("LANGUAGES");
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lang = langs[i];
                Log.d(LOG_TAG, lang);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buttonFind = findViewById(R.id.buttonFind);
        buttonRec = findViewById(R.id.buttonRec);
        dialog = new Dialog(MainActivity.this);


        buttonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogActivity(lang).show(getSupportFragmentManager(), FIND_TAG);
            }
        });

        buttonRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogActivity(lang).show(getSupportFragmentManager(), REC_TAG);
            }
        });
    }




}
