package com.example.madcamp1st.images;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final Activity activity;
    private List<Image> imageFilepaths;

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageButton imageButton;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton_images);
        }
    }

    public ImageAdapter(Activity activity, List<Image> imageFilepaths) {
        this.activity = activity;
        this.imageFilepaths = imageFilepaths;
    }

    public void updateImages(List<Image> imageFilepaths){
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
        Image image = imageFilepaths.get(position);
        Bitmap bitmap = Fragment_Images.decodeThumbnailFromFile(image.thumbnail.getPath(), 720, 1480);
        holder.imageButton.setImageBitmap(bitmap);
        holder.imageButton.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, FullImageActivity.class).putExtra("image path", image.thumbnail.getPath()))
        );
    }

    @Override
    public int getItemCount() {
        return imageFilepaths.size();
    }
}