package com.example.little_share.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ImageSaver {
    private static final String TAG = "ImageSaver";

    public static boolean saveBitmapToGallery(Context context, Bitmap bitmap, String fileName, String description) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore
                return saveBitmapToMediaStore(context, bitmap, fileName, description);
            } else {
                // Android 9 and below - Use External Storage
                return saveBitmapToExternalStorage(context, bitmap, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to gallery", e);
            return false;
        }
    }

    private static boolean saveBitmapToMediaStore(Context context, Bitmap bitmap, String fileName, String description) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LittleShare");
        
        if (description != null) {
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, description);
        }

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        
        if (imageUri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error writing to MediaStore", e);
                // Clean up failed insert
                resolver.delete(imageUri, null, null);
            }
        }
        
        return false;
    }

    private static boolean saveBitmapToExternalStorage(Context context, Bitmap bitmap, String fileName) {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDir = new File(picturesDir, "LittleShare");
        
        if (!appDir.exists() && !appDir.mkdirs()) {
            Log.e(TAG, "Failed to create directory");
            return false;
        }
        
        File imageFile = new File(appDir, fileName);
        
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            
            // Notify media scanner
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving to external storage", e);
            return false;
        }
    }
}