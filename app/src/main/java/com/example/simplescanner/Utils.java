package com.example.simplescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    static public File createImageFile(File directory) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SimpleScanner_" + timeStamp + "_";
        return File.createTempFile(imageFileName,".jpg", directory);
    }

    static public Bitmap getImage(String filepath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(filepath, bmOptions);
    }

    static public Bitmap getScaledImage(String filepath, DisplayMetrics display) {
        int displayWidth = display.widthPixels;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, bmOptions);
        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        int scaleFactor = Math.max(1, photoWidth / displayWidth);
        Log.d("Utils.getScaledImage", "Get scaled image with original size " + photoWidth + ", " + photoHeight + " optimized for display width " + displayWidth + " with scale factor " + scaleFactor);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(filepath, bmOptions);
    }
}
