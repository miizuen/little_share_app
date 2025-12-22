package com.example.little_share.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.little_share.R;

public class ImageViewerDialog extends Dialog {
    private ImageView ivFullImage;
    private ProgressBar progressBar;
    private ImageButton btnClose;
    private String imageUrl;
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;

    public ImageViewerDialog(Context context, String imageUrl) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.imageUrl = imageUrl;
        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.dialog_image_viewer);
        
        ivFullImage = findViewById(R.id.ivFullImage);
        progressBar = findViewById(R.id.progressBar);
        btnClose = findViewById(R.id.btnClose);

        // Load image
        loadImage();
        
        // Setup zoom gesture
        setupZoomGesture();
        
        // Close button
        btnClose.setOnClickListener(v -> dismiss());
        
        // Click outside to close
        findViewById(android.R.id.content).setOnClickListener(v -> dismiss());
    }

    private void loadImage() {
        if (imageUrl != null && !imageUrl.trim().equals("")) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.logo_d_japan) // placeholder
                    .error(R.drawable.logo_d_japan) // error image
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivFullImage);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupZoomGesture() {
        scaleDetector = new ScaleGestureDetector(getContext(), 
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scaleFactor *= detector.getScaleFactor();
                    
                    // Giới hạn zoom từ 0.5x đến 3x
                    scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
                    
                    ivFullImage.setScaleX(scaleFactor);
                    ivFullImage.setScaleY(scaleFactor);
                    return true;
                }
            });
        
        ivFullImage.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            
            // Double tap to reset zoom
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                if (event.getPointerCount() == 1) {
                    // Single tap - close dialog
                    v.performClick();
                    dismiss();
                }
            }
            
            return true;
        });
    }
}