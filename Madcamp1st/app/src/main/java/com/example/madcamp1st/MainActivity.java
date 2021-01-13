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

    public boolean isLoggedIn = false;
    public String fid = "";

    private Button internetButton;
    private boolean isConnected = false;

    public void setCurrentConnected(boolean isConnected){
        if(this.isConnected != isConnected) {
            if(isConnected) {
                internetButton.setText("Online");
                internetButton.setTextColor(Color.GREEN);
                if(mAdapter.fragment_diary != null && mAdapter.fragment_diary.reject != null) {
                    mAdapter.fragment_diary.reject.setVisibility(View.INVISIBLE);
                    if(!mAdapter.fragment_diary.hasInit)
                        mAdapter.fragment_diary._onCreateView();
                }
            }
            else {
                internetButton.setText("Offline");
                internetButton.setTextColor(Color.RED);
                if(mAdapter.fragment_diary != null && mAdapter.fragment_diary.reject != null)
                    mAdapter.fragment_diary.reject.setVisibility(View.VISIBLE);
            }

            this.isConnected = isConnected;
            mAdapter.notifyDataSetChanged();
        }
    }

    private void reconnectToInternet() {
        if(!isLoggedIn)
            setCurrentConnected(false);

        if(mAdapter.fragment_contacts != null)
            mAdapter.fragment_contacts.syncContacts();

        if(mAdapter.fragment_images != null)
            mAdapter.fragment_images.syncImages();
    }

    public void reconnectToInternet(View view){
        if(!isLoggedIn)
            Toast.makeText(this, "로그인해주세요", Toast.LENGTH_SHORT).show();

        reconnectToInternet();
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
                if(currentProfile != null) {
                    fid = currentProfile.getId();
                    isLoggedIn = true;
                    actionBar.setTitle(currentProfile.getName());
                } else {
                    isLoggedIn = false;
                    actionBar.setTitle(R.string.app_name);
                }

                reconnectToInternet();
            }
        };

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null && !accessToken.isExpired()) {
            Profile profile = Profile.getCurrentProfile();

            if(profile != null){
                fid = profile.getId();
                isLoggedIn = true;
                reconnectToInternet();
            }

            actionBar.setTitle(Profile.getCurrentProfile().getName());
        } else
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