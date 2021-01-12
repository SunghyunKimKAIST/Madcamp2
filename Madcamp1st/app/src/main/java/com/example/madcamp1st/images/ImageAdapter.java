package com.example.madcamp1st.images;

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
    private final Fragment_Images fragment_images;
    private List<Image> imageFilepaths;

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageButton imageButton;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton_images);
        }
    }

    public ImageAdapter(Fragment_Images fragment_images, List<Image> imageFilepaths) {
        this.fragment_images = fragment_images;
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
                fragment_images.startActivityForResult(new Intent(v.getContext(), FullImageActivity.class)
                        .putExtra("path", image.original.getPath())
                        .putExtra("name", image.name)
                        .putExtra("position", position), Fragment_Images.REQUEST_FULL_IMAGE)
        );
    }

    @Override
    public int getItemCount() {
        return imageFilepaths.size();
    }
}