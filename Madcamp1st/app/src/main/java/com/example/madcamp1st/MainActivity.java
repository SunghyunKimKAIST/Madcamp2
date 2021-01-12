package com.example.madcamp1st;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ProfileTracker profileTracker;

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

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if(currentProfile != null)
                    actionBar.setTitle(currentProfile.getName());
                else
                    actionBar.setTitle(R.string.app_name);
            }
        };

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

            if (accessToken == null || accessToken.isExpired()){
                Toast.makeText(this, "Facebook 로그인이 정상적으로 되지 않았습니다", Toast.LENGTH_SHORT).show();

                startActivityForResult(new Intent(this, LogInActivity.class), REQUEST_FACEBOOK_LOGIN);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu) ;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            startActivityForResult(new Intent(this, LogInActivity.class), REQUEST_FACEBOOK_LOGIN);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.startTracking();
    }
}