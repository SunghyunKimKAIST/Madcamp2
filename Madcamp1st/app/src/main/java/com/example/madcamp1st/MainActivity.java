package com.example.madcamp1st;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.madcamp1st.R.layout.activity_main);

        boolean permissionReadContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        BasePermissionListener basePermissionsListener = new BasePermissionListener(){
            @Override public void onPermissionGranted(PermissionGrantedResponse response){
                createView();
            }

            @Override public void onPermissionDenied(PermissionDeniedResponse response){
                createView();
            }
        };

        if(!permissionReadContacts && !permissionReadExternalStorage)
            Dexter.withContext(this)
                    .withPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new BaseMultiplePermissionsListener(){
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            createView();
                        }
                    }).check();
        else if(!permissionReadContacts)
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.READ_CONTACTS)
                    .withListener(basePermissionsListener)
                    .check();
        else if(!permissionReadExternalStorage)
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(basePermissionsListener)
                    .check();
        else
            createView();
    }

    private void createView() {
        ViewPager2 mViewPager = findViewById(R.id.viewPager_main);
        SectionPageAdapter adapter = new SectionPageAdapter(this);

        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout_main);
        new TabLayoutMediator(tabLayout, mViewPager, (tab, position) -> {
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