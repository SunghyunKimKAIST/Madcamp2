package com.example.madcamp1st;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private SectionPageAdapter mAdapter;
    private ActionBar actionBar;
    private ProfileTracker profileTracker;

    @StringRes private static final int[] TAB_TITLES = new int[] {R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final int REQUEST_FACEBOOK_LOGIN = 0;

    private Button internetButton;
    private boolean isConnected = false;

    public void setCurrentConnected(boolean isConnected){
        if(this.isConnected != isConnected) {
            if(isConnected) {
                internetButton.setText("Online");
                //internetButton.setBackgroundColor(Color.GREEN);
            }
            else {
                internetButton.setText("Offline");
                //internetButton.setBackgroundColor(Color.GREEN);
            }
        }
        this.isConnected = isConnected;
    }

    public void reconnectToInternet(View view){
        if(!isConnected){
            mAdapter.fragment_contacts.syncContacts();
            mAdapter.fragment_images.syncImages();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.madcamp1st.R.layout.activity_main);

        internetButton = findViewById(R.id.internet_button);

        actionBar = getSupportActionBar();

        ViewPager2 mViewPager = findViewById(R.id.viewPager_main);
        mAdapter = new SectionPageAdapter(this);
        mViewPager.setAdapter(mAdapter);

        new TabLayoutMediator(findViewById(R.id.tabLayout_main), mViewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])).attach();

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
            if(resultCode != RESULT_OK) {
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