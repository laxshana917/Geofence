package com.laxshana.project.geofence;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;


import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    Context context;
    ArrayList<String> imageUrls;

    public ImageAdapter(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteBtn = itemView.findViewById(R.id.deleteButton);
        }
    }

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        Glide.with(context).load(imageUrl).into(holder.imageView);


        // Handle click
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullImageActivity.class);
            intent.putExtra("image_url", imageUrl);
            context.startActivity(intent);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            photoRef.delete().addOnSuccessListener(unused -> {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                imageUrls.remove(position);
                notifyItemRemoved(position);
            });
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}


