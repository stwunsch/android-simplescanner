package com.example.simplescanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getClass().getSimpleName(), "Clicked button to take photo");
                try {
                    Toast.makeText(MainActivity.this, "TODO: Take photo", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Log.d(getClass().getSimpleName(), "Failed to take photo: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to take photo", Toast.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getClass().getSimpleName(), "Clicked button to save document");
                try {
                    Toast.makeText(MainActivity.this, "TODO: Save document", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Log.d(getClass().getSimpleName(), "Failed to save document: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to save document", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}