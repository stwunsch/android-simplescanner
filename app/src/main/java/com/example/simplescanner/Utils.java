package com.example.simplescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.android.ConvertBitmap;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;

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

    static public double getDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    static public Point getPointCoordinatesInImage(Point p, ImageView view, Bitmap image) {
        int inputHeight = view.getMeasuredHeight();
        int inputWidth = view.getMeasuredWidth();
        int outputHeight = image.getHeight();
        int outputWidth = image.getWidth();
        int x = (int) (p.x / (float) inputWidth * outputWidth);
        int y = (int) (p.y / (float) inputHeight * outputHeight);
        return new Point(x, y);
    }

    static public Bitmap getCroppedImage(Bitmap image, List<Point> points, ImageView view) throws Exception {
        Planar<GrayU8> color = ConvertBitmap.bitmapToPlanar(image, null, GrayU8.class, null);
        Point topLeft = getPointCoordinatesInImage(points.get(0), view, image);
        Point topRight = getPointCoordinatesInImage(points.get(1), view, image);
        Point bottomRight = getPointCoordinatesInImage(points.get(2), view, image);
        Point bottomLeft = getPointCoordinatesInImage(points.get(3), view, image);
        double outputWidth = Math.max(getDistance(topLeft, topRight), getDistance(bottomLeft, bottomRight));
        double outputHeight = Math.max(getDistance(topLeft, bottomLeft), getDistance(topRight, bottomRight));
        RemovePerspectiveDistortion removePerspective = new RemovePerspectiveDistortion((int)outputWidth, (int)outputHeight, ImageType.pl(3, GrayU8.class));
        if(!removePerspective.apply(color,
                new Point2D_F64(topLeft.x, topLeft.y), new Point2D_F64(topRight.x, topRight.y),
                new Point2D_F64(bottomRight.x, bottomRight.y), new Point2D_F64(bottomLeft.x, bottomLeft.y)))
        {
            throw new Exception("Failed to correct perspective");
        }
        Planar<GrayU8> output = (Planar<GrayU8>) removePerspective.getOutput();
        Bitmap editedImage = Bitmap.createBitmap(output.getWidth(), output.getHeight(), image.getConfig());
        ConvertBitmap.planarToBitmap(output, editedImage, null);
        return editedImage;
    }

    static public void writeImage(Bitmap image, String path) throws IOException {
        File file = new File(path);
        if (!file.delete()) {
            throw new IOException("Failed to delete file " + path);
        }
        if (!file.createNewFile()) {
            throw new IOException("Failed to create new file " + path);
        }
        FileOutputStream fileOut = new FileOutputStream(file);
        image.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
        fileOut.flush();
        fileOut.close();
    }

    static public String getFilename(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return prefix + "_" + timeStamp;
    }
}
