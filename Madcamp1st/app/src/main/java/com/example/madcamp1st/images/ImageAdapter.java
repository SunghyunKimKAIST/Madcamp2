package com.example.madcamp1st.images;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<File> imageFilepaths;

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_images);
        }
    }

    public ImageAdapter(List<File> imageFilepaths) {
        this.imageFilepaths = imageFilepaths;
    }

    public void updateImages(List<File> imageFilepaths){
        this.imageFilepaths = imageFilepaths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View imageView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.imageview_images, parent, false);

        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        File imageFilepath = imageFilepaths.get(position);

        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(imageFilepath.getPath()));
    }

    @Override
    public int getItemCount() {
        return imageFilepaths.size();
    }
}