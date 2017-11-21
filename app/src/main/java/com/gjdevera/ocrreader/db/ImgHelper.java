package com.gjdevera.ocrreader.db;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

/**
 * Created by Jacob on 11/20/2017.
 */

public class ImgHelper {
    public static final String TAG = "ImgHelper";

    public static Bitmap getCorrectedImageOrientation(String path) {
        Bitmap bmp = null;
        try { // populate image view with correct orientation
            bmp = BitmapFactory.decodeFile(path);
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return bmp;
    }
}
