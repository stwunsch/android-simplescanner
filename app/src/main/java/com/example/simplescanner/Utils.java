package com.example.simplescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.misc.PixelMath;
import boofcv.android.ConvertBitmap;
import boofcv.struct.ConfigLength;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;

public class Utils {

    static public void clearDirectory(File directory) {
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getName();
            Log.d("Utils.clearDirectory", "Remove file " + filename);
            files[i].delete();
        }
    }

    static public void rotateImage(String filepath) throws IOException {
        Bitmap image = getImage(filepath);
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap editedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        writeImage(editedImage, filepath);
    }

    static public File createImageFile(File directory) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SimpleScanner_" + timeStamp + "_";
        File file = File.createTempFile(imageFileName,".jpg", directory);
        return file;
    }

    static public Bitmap getImage(String filepath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(filepath, bmOptions);
    }

    static public Bitmap getGrayscaledImage(Bitmap image) {
        GrayU8 color = ConvertBitmap.bitmapToGray(image, (GrayU8) null,null);
        return ConvertBitmap.grayToBitmap(color, image.getConfig());
    }

    static public Bitmap getThresholdedImage(Bitmap image) {
        GrayU8 color = ConvertBitmap.bitmapToGray(image, (GrayU8) null,null);
        GrayU8 transformed = color.createSameShape();
        GThresholdImageOps.localSauvola(color, transformed,  ConfigLength.fixed(15), 0.30f, false);
        PixelMath.multiply(transformed, 255, transformed);
        return ConvertBitmap.grayToBitmap(transformed, image.getConfig());
    }

    static public Bitmap getSharpenedImage(Bitmap image) throws Exception {
        Planar<GrayU8> color = ConvertBitmap.bitmapToPlanar(image, null, GrayU8.class, null);
        if (color.getNumBands() != 3) {
            throw new Exception("Image does not have three color channels");
        }
        Planar<GrayU8> adjusted = color.createSameShape();
        for (int i = 0; i < 3; i++) {
            // TODO: Make sharpen configurable between sharpen4 and sharpen8
            EnhanceImageOps.sharpen4(color.getBand(i), adjusted.getBand(i));
        }
        Bitmap editedImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
        ConvertBitmap.planarToBitmap(adjusted, editedImage, null);
        return editedImage;
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
