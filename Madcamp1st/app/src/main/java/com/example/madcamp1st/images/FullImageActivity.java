package com.example.madcamp1st.images;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madcamp1st.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class FullImageActivity extends AppCompatActivity {
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        Intent intent = getIntent();

        ((PhotoView)findViewById(R.id.full_imageView)).setImageBitmap(BitmapFactory.decodeFile(intent.getStringExtra("path")));

        getSupportActionBar().setTitle(intent.getStringExtra("name"));

        position = intent.getIntExtra("position", -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            Intent intent = new Intent();
            intent.putExtra("deleted", position);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}