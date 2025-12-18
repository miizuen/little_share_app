package com.example.little_share.helper;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgBBUploader {
    private static final String TAG = "ImgBBUploader";
    private static final String API_KEY = "4d3af58f894f5b65554f182de82fc6cc";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onFailure(String error);
        void onProgress(int progress);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadListener listener) {
        new Thread(() -> {
            try {
                // Đọc file thành byte array
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();

                // Chuyển thành Base64
                String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                // Tạo request body
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("key", API_KEY)
                        .addFormDataPart("image", base64Image)
                        .build();

                // Tạo request
                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .post(requestBody)
                        .build();

                // Upload
                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, java.io.IOException e) {
                        Log.e(TAG, "Upload failed: " + e.getMessage());
                        listener.onFailure(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws java.io.IOException {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                JSONObject json = new JSONObject(responseBody);

                                if (json.getBoolean("success")) {
                                    String imageUrl = json.getJSONObject("data").getString("url");
                                    Log.d(TAG, "Upload success: " + imageUrl);
                                    listener.onSuccess(imageUrl);
                                } else {
                                    listener.onFailure("Upload failed");
                                }
                            } catch (Exception e) {
                                listener.onFailure("Parse error: " + e.getMessage());
                            }
                        } else {
                            listener.onFailure("HTTP " + response.code());
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                listener.onFailure(e.getMessage());
            }
        }).start();
    }
}