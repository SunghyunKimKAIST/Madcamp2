package com.example.madcamp1st.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.BasePermissionListener;

import java.util.ArrayList;

public class Fragment_Contacts extends Fragment {
    private View mView;
    private ContactAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            onPermissionGranted_contacts();
        else{
            view.findViewById(R.id.button_request_contacts).setOnClickListener(v -> {
                Dexter.withContext(getContext())
                        .withPermission(Manifest.permission.READ_CONTACTS)
                        .withListener(new BasePermissionListener(){
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                onPermissionGranted_contacts();
                            }
                        }).check();
            });
        }
    }

    private void onPermissionGranted_contacts() {
        mView.findViewById(R.id.reject_contacts).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.relativeLayout_contacts).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.floatingActionButton_contacts).setVisibility(View.VISIBLE);

        showContacts();

        SearchView searchView = mView.findViewById(R.id.searchView_contacts);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });

        mView.findViewById(R.id.floatingActionButton_contacts).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_INSERT, Uri.parse("content://contacts/people"));
            startActivity(intent);
        });
    }

    private Contact[] loadContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();

        Cursor c = getContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null, null);

        int nameColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numberColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        while (c.moveToNext()) {
            String contactName = c.getString(nameColumn);
            String phNumber = c.getString(numberColumn);

            contacts.add(new Contact(contactName, phNumber));
        }
        c.close();

        return contacts.toArray(new Contact[0]);
    }

    private void showContacts() {
        Contact[] contacts = loadContacts();

        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView_contacts);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ContactAdapter(contacts);
        recyclerView.setAdapter(mAdapter);
    }

    private void uploadContacts(Contact[] contacts){

    }
}