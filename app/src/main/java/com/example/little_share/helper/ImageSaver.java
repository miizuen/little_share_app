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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class để lưu ảnh QR code vào thư viện ảnh của thiết bị
 * Hỗ trợ cả Android 10+ (MediaStore) và phiên bản cũ hơn (External Storage)
 */
public class ImageSaver {
    private static final String TAG = "ImageSaver";
    private static final String FOLDER_NAME = "LittleShare";
    
    public interface OnImageSavedListener {
        void onSuccess(String filePath);
        void onFailure(String error);
    }
    
    /**
     * Lưu bitmap QR code vào thư viện ảnh
     * @param context Context của activity/fragment
     * @param bitmap Bitmap của QR code cần lưu
     * @param fileName Tên file (không bao gồm extension)
     * @param listener Callback để nhận kết quả
     */
    public static void saveQRCodeToGallery(Context context, Bitmap bitmap, String fileName, OnImageSavedListener listener) {
        if (bitmap == null) {
            if (listener != null) {
                listener.onFailure("Bitmap QR code không hợp lệ");
            }
            return;
        }
        
        // Tạo tên file với timestamp để tránh trùng lặp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String finalFileName = fileName + "_" + timestamp + ".png";
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Sử dụng MediaStore
                saveToMediaStore(context, bitmap, finalFileName, listener);
            } else {
                // Android 9 và thấp hơn - Sử dụng External Storage
                saveToExternalStorage(context, bitmap, finalFileName, listener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu QR code: " + e.getMessage(), e);
            if (listener != null) {
                listener.onFailure("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lưu ảnh sử dụng MediaStore (Android 10+)
     */
    private static void saveToMediaStore(Context context, Bitmap bitmap, String fileName, OnImageSavedListener listener) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + FOLDER_NAME);
        
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        
        if (imageUri == null) {
            if (listener != null) {
                listener.onFailure("Không thể tạo file trong thư viện ảnh");
            }
            return;
        }
        
        try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
            if (outputStream == null) {
                if (listener != null) {
                    listener.onFailure("Không thể mở stream để ghi file");
                }
                return;
            }
            
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            
            if (success) {
                Log.d(TAG, "QR code đã được lưu thành công vào MediaStore: " + imageUri.toString());
                if (listener != null) {
                    listener.onSuccess(imageUri.toString());
                }
            } else {
                if (listener != null) {
                    listener.onFailure("Lỗi khi nén và lưu ảnh");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu vào MediaStore: " + e.getMessage(), e);
            if (listener != null) {
                listener.onFailure("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lưu ảnh sử dụng External Storage (Android 9 và thấp hơn)
     */
    private static void saveToExternalStorage(Context context, Bitmap bitmap, String fileName, OnImageSavedListener listener) {
        // Tạo thư mục Pictures/LittleShare
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDir = new File(picturesDir, FOLDER_NAME);
        
        if (!appDir.exists()) {
            boolean created = appDir.mkdirs();
            if (!created) {
                Log.w(TAG, "Không thể tạo thư mục " + FOLDER_NAME);
            }
        }
        
        File imageFile = new File(appDir, fileName);
        
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            
            if (success) {
                // Thông báo cho MediaScanner để ảnh xuất hiện trong thư viện
                android.media.MediaScannerConnection.scanFile(
                    context,
                    new String[]{imageFile.getAbsolutePath()},
                    new String[]{"image/png"},
                    null
                );
                
                Log.d(TAG, "QR code đã được lưu thành công: " + imageFile.getAbsolutePath());
                if (listener != null) {
                    listener.onSuccess(imageFile.getAbsolutePath());
                }
            } else {
                if (listener != null) {
                    listener.onFailure("Lỗi khi nén và lưu ảnh");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu vào External Storage: " + e.getMessage(), e);
            if (listener != null) {
                listener.onFailure("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        }
    }
    
    /**
     * Kiểm tra xem có thể lưu ảnh vào external storage không
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}