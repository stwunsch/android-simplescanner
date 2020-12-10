package com.example.simplescanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int EDIT_IMAGE = 2;

    private File photoDirectory;
    private String currentPhotoPath;
    private int nextViewId;

    private void dispatchPhotoIntent()  {
        Log.d(getClass().getSimpleName(), "Dispatch photo intent");

        PackageManager packageManager = this.getPackageManager();
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Toast.makeText(MainActivity.this, "No camera available to take photo", Toast.LENGTH_LONG).show();
            return;
        }

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(MainActivity.this, "No camera activity available to take photo", Toast.LENGTH_LONG).show();
            return;
        }

        File photoFile;
        try {
            photoFile = Utils.createImageFile(photoDirectory);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Failed to create image file", Toast.LENGTH_LONG).show();
            return;
        }
        currentPhotoPath = photoFile.getAbsolutePath();
        Uri photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        nextViewId = 1000;

        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getClass().getSimpleName(), "Clicked button to take photo");
                try {
                    dispatchPhotoIntent();
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

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    protected void addGalleryEntry() {
        Log.d(getClass().getSimpleName(), "Add gallery entry for photo " + currentPhotoPath);
        LinearLayout gallery = findViewById(R.id.gallery);
        RelativeLayout imageLayout = new RelativeLayout(this);
        gallery.addView(imageLayout);

        // Attach the original filename to a dummy textview
        // TODO: Make this a proper member variable of the layout
        TextView photoPath = new TextView(this);
        photoPath.setText(currentPhotoPath);
        photoPath.setVisibility(View.GONE);
        imageLayout.addView(photoPath);

        ImageView imageView = new ImageView(this);
        imageView.setId(nextViewId++);
        RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageView.setLayoutParams(paramsImageView);
        DisplayMetrics display = getDisplayMetrics();
        Bitmap image = Utils.getScaledImage(currentPhotoPath, display);
        imageView.setImageBitmap(image);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(50, 0, 50, 0); // TODO: Optimize the layout
        imageLayout.addView(imageView);

        ImageButton editButton = new ImageButton(this);
        editButton.setId(nextViewId++);
        RelativeLayout.LayoutParams paramsEditButton = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsEditButton.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
        editButton.setLayoutParams(paramsEditButton);
        imageLayout.addView(editButton);
        editButton.setImageResource(android.R.drawable.ic_menu_crop);
        editButton.setBackgroundColor(Color.TRANSPARENT);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String thisPhoto = photoPath.getText().toString();
                Log.d(getClass().getSimpleName(), "Clicked button to edit photo " + thisPhoto);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("photoPath", thisPhoto);
                startActivityForResult(intent, EDIT_IMAGE);
            }
        });

        ImageButton removeButton = new ImageButton(this);
        RelativeLayout.LayoutParams paramsDeleteButton = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsDeleteButton.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
        paramsDeleteButton.addRule(RelativeLayout.BELOW, editButton.getId());
        removeButton.setLayoutParams(paramsDeleteButton);
        imageLayout.addView(removeButton);
        removeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        removeButton.setBackgroundColor(Color.TRANSPARENT);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getClass().getSimpleName(), "Remove from gallery the image " + currentPhotoPath);
                imageLayout.removeAllViews();
                gallery.removeView(imageLayout);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getClass().getSimpleName(), "On activity result with request code " + requestCode + " and result code " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(getClass().getSimpleName(), "Handle on activity result of photo intent with file " + currentPhotoPath);
            addGalleryEntry();
        } else {
            Log.d(getClass().getSimpleName(), "On activity result could not be handled");
        }
    }
}