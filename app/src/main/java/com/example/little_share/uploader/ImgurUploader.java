package com.example.little_share.uploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgurUploader {
    private static final String TAG = "ImgurUploader";
    // Client ID công khai của Imgur - bạn nên đăng ký của riêng mình tại: https://api.imgur.com/oauth2/addclient
    private static final String CLIENT_ID = "546c25a59c58ad7";
    private static final String UPLOAD_URL = "https://api.imgur.com/3/image";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
        void onProgress(int progress);
    }

    /**
     * Upload ảnh lên Imgur
     * @param context Context của Activity
     * @param imageUri URI của ảnh cần upload
     * @param callback Callback để nhận kết quả
     */
    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting upload...");

                // Bước 1: Đọc ảnh từ URI
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onFailure("Không thể đọc ảnh"));
                    return;
                }

                // Bước 2: Nén ảnh để giảm kích thước (tránh quá 10MB)
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (originalBitmap == null) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onFailure("Ảnh không hợp lệ"));
                    return;
                }

                // Resize ảnh nếu quá lớn
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 1920, 1920);

                // Convert sang byte array với chất lượng 85%
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Giải phóng bộ nhớ
                originalBitmap.recycle();
                resizedBitmap.recycle();

                Log.d(TAG, "Image size: " + (imageBytes.length / 1024) + " KB");

                // Progress 30%
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onProgress(30));

                // Bước 3: Convert sang Base64
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                // Progress 50%
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onProgress(50));

                // Bước 4: Tạo request upload
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", base64Image)
                        .addFormDataPart("type", "base64")
                        .build();

                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .addHeader("Authorization", "Client-ID " + CLIENT_ID)
                        .post(requestBody)
                        .build();

                // Progress 70%
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onProgress(70));

                Log.d(TAG, "Sending request to Imgur...");

                // Bước 5: Gửi request
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response body: " + responseBody);

                // Progress 90%
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onProgress(90));

                if (response.isSuccessful()) {
                    // Parse JSON để lấy URL ảnh
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.getBoolean("success")) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        String imageUrl = data.getString("link");

                        Log.d(TAG, "Upload success! URL: " + imageUrl);

                        // Callback success
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            callback.onProgress(100);
                            callback.onSuccess(imageUrl);
                        });
                    } else {
                        ((android.app.Activity) context).runOnUiThread(() ->
                                callback.onFailure("Upload thất bại: Response không thành công"));
                    }
                } else {
                    Log.e(TAG, "Upload failed with code: " + response.code());
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onFailure("Lỗi server: " + response.code() + " - " + responseBody));
                }

            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onFailure("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Resize bitmap để giảm kích thước
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Nếu ảnh đã nhỏ hơn max size thì không cần resize
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scale = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
