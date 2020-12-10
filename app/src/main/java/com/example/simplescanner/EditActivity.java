package com.example.simplescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {

    private String currentPhotoPath;
    private Bitmap originalImage;
    private Bitmap editedImage;

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Edit photo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentPhotoPath = intent.getStringExtra("photoPath");
        Log.d("EditActivity", "Start activity to edit photo " + currentPhotoPath);

        BoxImageView previewView = findViewById(R.id.previewView);
        originalImage = Utils.getImage(currentPhotoPath);
        editedImage = Utils.getImage(currentPhotoPath);
        previewView.setImageBitmap(editedImage);
        previewView.setAdjustViewBounds(true);
    }
}