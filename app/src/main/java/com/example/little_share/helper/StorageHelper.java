package com.example.little_share.helper;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageHelper {
    private static final String STORAGE_BUCKET = "gs://littleshare-7b64c.firebasestorage.app";
    private static FirebaseStorage storageInstance;

    public static FirebaseStorage getStorage() {
        if (storageInstance == null) {
            storageInstance = FirebaseStorage.getInstance(STORAGE_BUCKET);
        }
        return storageInstance;
    }

    public static StorageReference getCampaignImageRef(String fileName) {
        return getStorage().getReference().child("campaigns/" + fileName);
    }

    public static void uploadCampaignImage(Uri imageUri, String fileName,
                                           OnUploadListener listener) {
        StorageReference ref = getCampaignImageRef(fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (listener != null) {
                            listener.onSuccess(uri.toString());
                        }
                    }).addOnFailureListener(e -> {
                        if (listener != null) {
                            listener.onFailure(e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public interface OnUploadListener {
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }
}
