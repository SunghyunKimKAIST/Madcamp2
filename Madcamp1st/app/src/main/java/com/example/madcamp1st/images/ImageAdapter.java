package com.example.madcamp1st.images;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final Fragment_Images fragment_images;
    private final Image[] images;

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageButton imageButton;
        public TextView name;

        public ImageViewHolder(View imageView) {
            super(imageView);

            imageButton = imageView.findViewById(R.id.imageButton_imagebuttonAndTextview);
            name = imageView.findViewById(R.id.textView_imagebuttonAndTextview);
        }
    }

    public ImageAdapter(Fragment_Images fragment_images, Image[] images) {
        this.fragment_images = fragment_images;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View imageView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.imagebutton_and_textview, parent, false);

        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Uri uri = images[position].uri;
        String name = images[position].name;

        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment_images.zoomImageFromThumb(holder.imageButton, uri);
            }
        });

        try {
            Bitmap thumbnail = fragment_images.getContext().getContentResolver().loadThumbnail(uri, new Size(640, 480), null);
            holder.imageButton.setImageBitmap(thumbnail);
            holder.name.setText(name);
        } catch (Exception e) {
            holder.imageButton.setImageResource(R.drawable.sorry2);
        }
    }

    @Override
    public int getItemCount() {
        return images.length;
    }
}