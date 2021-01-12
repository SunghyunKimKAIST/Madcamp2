package com.example.madcamp1st;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private ActionBar actionBar;

    private final int REQUEST_FACEBOOK_LOGIN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.madcamp1st.R.layout.activity_main);

        actionBar = getSupportActionBar();

        ViewPager2 mViewPager = findViewById(R.id.viewPager_main);
        mViewPager.setAdapter(new SectionPageAdapter(this));

        new TabLayoutMediator(findViewById(R.id.tabLayout_main), mViewPager, (tab, position) -> {
            ImageView imgView = new ImageView(this);

            switch (position) {
                case 0:
                    imgView.setImageResource(R.drawable.tab_icon_contacts);
                    break;
                case 1:
                    imgView.setImageResource(R.drawable.tab_icon_images);
                    break;
                case 2:
                    imgView.setImageResource(R.drawable.tab_icon_diary);
            }

            imgView.setPadding(10, 10, 10, 10);
            tab.setCustomView(imgView);
        }).attach();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null && !accessToken.isExpired())
            actionBar.setTitle(Profile.getCurrentProfile().getName());
        else
            startActivityForResult(new Intent(this, LogInActivity.class), REQUEST_FACEBOOK_LOGIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_FACEBOOK_LOGIN) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();

            if (accessToken != null && !accessToken.isExpired())
                actionBar.setTitle(Profile.getCurrentProfile().getName());
            else{
                Toast.makeText(this, "Facebook 로그인이 정상적으로 되지 않았습니다", Toast.LENGTH_SHORT).show();

                startActivityForResult(new Intent(this, LogInActivity.class), REQUEST_FACEBOOK_LOGIN);
            }
        }
    }
}