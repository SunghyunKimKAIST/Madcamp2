package com.example.madcamp1st.images;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.madcamp1st.R;

public class FullImageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        ((ImageView)findViewById(R.id.full_imageView)).setImageBitmap(BitmapFactory.decodeFile(getIntent().getStringExtra("image path")));
    }
}