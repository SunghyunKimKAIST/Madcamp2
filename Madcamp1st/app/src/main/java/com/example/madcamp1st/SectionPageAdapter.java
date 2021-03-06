package com.example.madcamp1st;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.madcamp1st.contacts.Fragment_Contacts;
import com.example.madcamp1st.diary.Fragment_Diary;
import com.example.madcamp1st.images.Fragment_Images;

public class SectionPageAdapter extends FragmentStateAdapter {
    public Fragment_Contacts fragment_contacts;
    public Fragment_Images fragment_images;
    public Fragment_Diary fragment_diary;

    public SectionPageAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                fragment_contacts = new Fragment_Contacts();
                return fragment_contacts;
            case 1:
                fragment_images = new Fragment_Images();
                return fragment_images;
            case 2:
                fragment_diary = new Fragment_Diary();
                return fragment_diary;
        }

        return new Fragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}