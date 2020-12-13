package com.example.simplescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private String currentPhotoPath;
    private Bitmap editedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Edit photo");

        Intent intent = getIntent();
        currentPhotoPath = intent.getStringExtra("photoPath");
        Log.d(getClass().getSimpleName(), "Start activity to edit photo " + currentPhotoPath);

        BoxImageView previewView = findViewById(R.id.previewView);
        editedImage = Utils.getImage(currentPhotoPath);
        previewView.setImageBitmap(editedImage);
        previewView.setAdjustViewBounds(true);

        Button buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editedImage = Utils.getImage(currentPhotoPath);
                previewView.resetBox();
                previewView.setImageBitmap(editedImage);
            }
        });

        Button buttonCrop = findViewById(R.id.buttonCrop);
        buttonCrop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                List<Point> points = previewView.getPoints();
                try {
                    editedImage = Utils.getCroppedImage(editedImage, points, previewView);
                } catch (Exception e) {
                    Toast.makeText(EditActivity.this, "Failed to crop image", Toast.LENGTH_LONG).show();
                }
                previewView.resetBox();
                previewView.setImageBitmap(editedImage);
            }
        });

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("photoPath", currentPhotoPath);
                try {
                    Utils.writeImage(editedImage, currentPhotoPath);
                    setResult(RESULT_OK, intent);
                } catch (IOException e) {
                    Log.d(getClass().getSimpleName(), "Failed to save edited photo: " + e.getMessage());
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
            }
        });
    }
}