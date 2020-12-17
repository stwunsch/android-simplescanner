package com.example.simplescanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.InvalidPreferencesFormatException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int EDIT_IMAGE = 2;
    static final int SAVE_DOCUMENT = 3;
    static final float INCH_PER_MM = 0.0393701f;

    private File photoDirectory;
    private String currentPhotoPath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.d(getClass().getSimpleName(), "Clicked in action bar on settings");
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == R.id.action_about) {
            Log.d(getClass().getSimpleName(), "Clicked in action bar on about");
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        } else if (id == R.id.action_save) {
            Log.d(getClass().getSimpleName(), "Clicked button to save document");
            LinearLayout gallery = findViewById(R.id.gallery);
            if (gallery.getChildCount() == 0) {
                Toast.makeText(MainActivity.this, "Nothing to save", Toast.LENGTH_LONG).show();
                return true;
            }
            try {
                dispatchSaveDocumentIntent();
            }
            catch (Exception e) {
                Log.d(getClass().getSimpleName(), "Failed to save document: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Failed to save document", Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (id == R.id.action_reset) {
            LinearLayout gallery = findViewById(R.id.gallery);
            gallery.removeAllViews();
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchSaveDocumentIntent() throws IOException {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String filename = Utils.getFilename("scan");
        intent.putExtra(Intent.EXTRA_TITLE, filename + ".pdf");
        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        startActivityForResult(intent, SAVE_DOCUMENT);
    }

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
    public void onDestroy() {
        super.onDestroy();
        Utils.clearDirectory(photoDirectory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

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

        // TODO: Make the image path a proper member variable of the layout
        TextView photoPath = new TextView(this);
        photoPath.setText(currentPhotoPath);
        photoPath.setVisibility(View.GONE);
        imageLayout.addView(photoPath);

        ImageView imageView = new ImageView(this);
        imageView.setId(View.generateViewId());
        RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageView.setLayoutParams(paramsImageView);
        DisplayMetrics display = getDisplayMetrics();
        Bitmap image = Utils.getScaledImage(currentPhotoPath, display);
        imageView.setImageBitmap(image);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageLayout.addView(imageView);

        ImageButton editButton = new ImageButton(this);
        editButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams paramsEditButton = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsEditButton.rightMargin = (int) getResources().getDimension(R.dimen.image_button_margin);
        paramsEditButton.topMargin = paramsEditButton.rightMargin;
        paramsEditButton.bottomMargin = paramsEditButton.rightMargin;
        paramsEditButton.height = (int) getResources().getDimension(R.dimen.image_button_size);
        paramsEditButton.width = paramsEditButton.height;
        paramsEditButton.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
        editButton.setLayoutParams(paramsEditButton);
        imageLayout.addView(editButton);
        editButton.setImageResource(R.drawable.ic_action_crop);
        editButton.setBackground(getResources().getDrawable(R.drawable.button_background));

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

        ImageButton rotateButton = new ImageButton(this);
        rotateButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams paramsRotateButton = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsRotateButton.addRule(RelativeLayout.BELOW, editButton.getId());
        paramsRotateButton.addRule(RelativeLayout.ALIGN_START, editButton.getId());
        paramsRotateButton.height = paramsEditButton.height;
        paramsRotateButton.width = paramsRotateButton.height;
        paramsRotateButton.bottomMargin = paramsEditButton.rightMargin;
        rotateButton.setLayoutParams(paramsRotateButton);
        imageLayout.addView(rotateButton);
        rotateButton.setImageResource(R.drawable.ic_action_rotate);
        rotateButton.setBackground(getResources().getDrawable(R.drawable.button_background));

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String thisPhoto = photoPath.getText().toString();
                Log.d(getClass().getSimpleName(), "Rotate in gallery the image " + thisPhoto);
                try {
                    Utils.rotateImage(thisPhoto);
                } catch (IOException e) {
                    Log.d(getClass().getSimpleName(), "Failed to rotate image: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to rotate image", Toast.LENGTH_LONG).show();
                }
                updateGallery(thisPhoto);
            }
        });

        ImageButton deleteButton = new ImageButton(this);
        RelativeLayout.LayoutParams paramsDeleteButton = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsDeleteButton.addRule(RelativeLayout.BELOW, rotateButton.getId());
        paramsDeleteButton.addRule(RelativeLayout.ALIGN_START, rotateButton.getId());
        paramsDeleteButton.height = paramsEditButton.height;
        paramsDeleteButton.width = paramsDeleteButton.height;
        deleteButton.setLayoutParams(paramsDeleteButton);
        imageLayout.addView(deleteButton);
        deleteButton.setImageResource(R.drawable.ic_action_delete);
        deleteButton.setBackground(getResources().getDrawable(R.drawable.button_background));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String thisPhoto = photoPath.getText().toString();
                Log.d(getClass().getSimpleName(), "Remove from gallery the image " + thisPhoto);
                imageLayout.removeAllViews();
                gallery.removeView(imageLayout);
            }
        });
    }

    private void updateGallery(String imagePath) {
        LinearLayout gallery = findViewById(R.id.gallery);
        for (int i = 0; i < gallery.getChildCount(); i++) {
            RelativeLayout imageLayout = (RelativeLayout) gallery.getChildAt(i);
            TextView text = (TextView) imageLayout.getChildAt(0);
            String path = text.getText().toString();
            if (path.equals(imagePath)) {
                Log.d(getClass().getSimpleName(), "Update view in gallery at position " + i);
                ImageView imageView = (ImageView) imageLayout.getChildAt(1);
                Bitmap image = Utils.getScaledImage(imagePath, getDisplayMetrics());
                imageView.setImageBitmap(image);
                return;
            }
        }
        Log.w(getClass().getSimpleName(), "Failed to update gallery");
    }

    private void saveDocument(Uri uri) throws IOException, InvalidPreferencesFormatException, NumberFormatException {
        PDFBoxResourceLoader.init(getApplicationContext());
        PDDocument document = new PDDocument();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String pageWidthStr = prefs.getString("page_width", null);
        String pageHeightStr = prefs.getString("page_height", null);
        if (pageWidthStr == null || pageHeightStr == null) {
            throw new InvalidPreferencesFormatException("Failed to read preferences page_width and page_height");
        }
        int pageWidth = Integer.parseInt(pageWidthStr.trim());
        int pageHeight = Integer.parseInt(pageHeightStr.trim());
        Log.d(getClass().getSimpleName(), "Load page size from prefs " + pageWidth + ", " + pageHeight);
        if (pageWidth <= 0 || pageHeight <= 0) {
            throw new InvalidPreferencesFormatException("Invalid number for preference page_width or page_height");
        }
        float pageWidthInch = Math.round(pageWidth / INCH_PER_MM / 72.f);
        float pageHeightInch = Math.round(pageHeight / INCH_PER_MM / 72.f);

        String pageResizeMode = prefs.getString("page_resize_mode", null);
        if (pageResizeMode == null) {
            throw new InvalidPreferencesFormatException("Failed to read preferences page_resize_mode");
        }

        String imageCompressionFactorStr = prefs.getString("image_compression_factor", null);
        if (imageCompressionFactorStr == null) {
            throw new InvalidPreferencesFormatException("Failed to read preferences image_compression_factor");
        }
        float imageCompressionFactor = Float.parseFloat(imageCompressionFactorStr.trim());

        LinearLayout gallery = findViewById(R.id.gallery);
        for (int i = 0; i < gallery.getChildCount(); i++) {
            Log.d(getClass().getSimpleName(), "Process image at position " + i);

            RelativeLayout layout = (RelativeLayout) gallery.getChildAt(i);
            TextView text = (TextView) layout.getChildAt(0);
            String path = text.getText().toString();
            Bitmap image =  Utils.getImage(path);
            float imageHeight = (float) image.getHeight();
            float imageWidth = (float) image.getWidth();

            float pageWidthFinal = pageWidthInch;
            float pageHeightFinal = pageHeightInch;
            if (pageResizeMode.equals("fit_width")) {
                Log.d(getClass().getSimpleName(), "Resize page width to content");
                pageWidthFinal = Math.round(pageHeightInch * imageWidth / imageHeight);
            } else if (pageResizeMode.equals("fit_height")) {
                Log.d(getClass().getSimpleName(), "Resize page height to content");
                pageHeightFinal = Math.round(pageWidthInch * imageHeight / imageWidth);
            } else {
                Log.d(getClass().getSimpleName(), "Resize content to page");
            }

            PDRectangle pageSize = new PDRectangle(0, 0, pageWidthFinal, pageHeightFinal);
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            PDPageContentStream stream = new PDPageContentStream(document, page);
            PDImageXObject ximage = JPEGFactory.createFromImage(document, image, imageCompressionFactor);
            stream.drawImage(ximage, 0, 0, pageSize.getWidth(), pageSize.getHeight());
            stream.close();
        }

        OutputStream stream = getContentResolver().openOutputStream(uri);
        document.save(stream);
        document.close();
        stream.close();
        Log.d(getClass().getSimpleName(), "Saved document");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getClass().getSimpleName(), "On activity result with request code " + requestCode + " and result code " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(getClass().getSimpleName(), "Handle on activity result of take photo intent with file " + currentPhotoPath);
            addGalleryEntry();
        } else if (requestCode == EDIT_IMAGE && resultCode == RESULT_OK) {
            String imagePath = data.getExtras().get("photoPath").toString();
            Log.d(getClass().getSimpleName(), "Handle on activity result of edit photo intent with file " + imagePath);
            updateGallery(imagePath);
        } else if (requestCode == SAVE_DOCUMENT && resultCode == RESULT_OK) {
            Log.d(getClass().getSimpleName(), "Handle on activity result of save document intent");
            Uri uri = data.getData();
            try {
                saveDocument(uri);
                Log.d(getClass().getSimpleName(), "Saved document to " + uri.getPath());
                String[] fullPath = uri.getPath().split(":");
                String path = "";
                if (fullPath.length == 2) {
                    path = " to " + fullPath[1];
                }
                Log.d(getClass().getSimpleName(), "Saved document" + path);
                Toast.makeText(MainActivity.this, "Saved document" + path, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.w(getClass().getSimpleName(), "Failed to save document: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Failed to save document", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "Failed to get preferences: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Invalid preferences", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(getClass().getSimpleName(), "On activity result could not be handled");
        }
    }
}