package com.example.madcamp1st;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.madcamp1st.R.layout.activity_main);

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
                    imgView.setImageResource(R.drawable.tab_icon_games);
            }
            imgView.setPadding(10, 10, 10, 10);
            tab.setCustomView(imgView);
        }).attach();
    }
}