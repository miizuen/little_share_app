package com.example.little_share.ui.ngo.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.little_share.R;
import com.example.little_share.data.models.ReportImage;

import java.util.List;

public class ReportImageAdapter extends RecyclerView.Adapter<ReportImageAdapter.ImageViewHolder> {

    public interface OnImageActionListener {
        void onRemove(int position);
    }

    private List<ReportImage> imageList;
    private OnImageActionListener listener;

    public ReportImageAdapter(List<ReportImage> imageList, OnImageActionListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ReportImage reportImage = imageList.get(position);
        holder.bind(reportImage, position);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPhoto, btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgReport);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
        }

        public void bind(ReportImage reportImage, int position) {
            // Load image using Glide
            if (reportImage.getImageUrl().startsWith("content://")) {
                // Local URI
                Glide.with(itemView.getContext())
                        .load(Uri.parse(reportImage.getImageUrl()))
                        .centerCrop()
                        .into(imgPhoto);
            } else {
                // Firebase URL
                Glide.with(itemView.getContext())
                        .load(reportImage.getImageUrl())
                        .centerCrop()
                        .into(imgPhoto);
            }

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(position);
                }
            });
        }
    }
}
