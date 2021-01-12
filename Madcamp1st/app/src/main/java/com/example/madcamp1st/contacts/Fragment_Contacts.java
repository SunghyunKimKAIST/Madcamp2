package com.example.madcamp1st.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.example.madcamp1st.DeleteCallback;
import com.example.madcamp1st.MyResponse;
import com.example.madcamp1st.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

public class Fragment_Contacts extends Fragment {
    private View mView;
    private ContactAdapter mAdapter;
    private Button internetButton;
    private boolean isConnected;

    private Gson gson;

    private ZonedDateTime timestamp;
    private final String contactsTimestampFileName = "contacts_timestamp";
    private final ZonedDateTime MIN_ZONED_DATE_TIME = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

    private List<Contact> internalContacts;
    private final String contactsDataFileName = "contacts_data";

    private final String DB_URL = "http://192.249.18.163:1234/";
    private ContactService contactService;

    private final int REQUEST_PICK_CONTACT = 0;

    private static class SyncFlag {
        public int n;
        public ZonedDateTime update;

        public SyncFlag(int n, ZonedDateTime update){
            this.n = n;
            this.update = update;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isConnected = false;
        internetButton = mView.findViewById(R.id.contact_internet_button);
        internetButton.setText("Disconnected");

        gson = new Gson();

        // get cashed data
        timestamp = loadTimestamp();
        internalContacts = loadContactsFromInternal();

        // recyclerview
        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new ContactAdapter(this, internalContacts);
        recyclerView.setAdapter(mAdapter);

        // DB 통신
        contactService = new Retrofit.Builder()
                .baseUrl(DB_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ContactService.class);

        contactService.getAllNewerContacts(timestamp.format(DateTimeFormatter.ISO_INSTANT)).enqueue(new Callback<List<Contact>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    List<Contact> dbContacts = response.body();

                    internalContacts = syncContacts(internalContacts, dbContacts);
                    mAdapter.updateContacts(internalContacts);

                    isConnected = true;
                    internetButton.setText("Connected");
                } else
                    Toast.makeText(getContext(), "getAllNewerContacts: DB에서 연락처를 불러오는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                Toast.makeText(getContext(), "getAllNewerContacts: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });

        // searchview
        ((SearchView)mView.findViewById(R.id.searchView_contacts)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.filter(query, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText, true);
                return true;
            }
        });

        mView.findViewById(R.id.button_create_contact).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_PICK_CONTACT);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            Cursor cursor = getContext().getContentResolver().query(
                    data.getData(),
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, null);

            if(cursor == null || !cursor.moveToFirst()){
                Toast.makeText(getContext(), "pick contact error", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            cursor.close();

            Contact contact = new Contact(name, number);

            internalContacts.add(contact);
            internalContacts.sort(null);
            mAdapter.updateContacts(internalContacts);

            createContactToDBAsync(contact, new SyncFlag(1, contact.getTimestamp()));
        }
    }

    private ZonedDateTime loadTimestamp() {
        File file = new File(getContext().getFilesDir(), contactsTimestampFileName);

        if (!file.exists())
            return MIN_ZONED_DATE_TIME;
        else {
            byte[] bytes;

            try{
                bytes = Files.readAllBytes(file.toPath());
            }catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "내부 저장소에서 Timestamp를 불러오는데 실패했습니다", Toast.LENGTH_LONG).show();
                return MIN_ZONED_DATE_TIME;
            }

            return ZonedDateTime.parse(new String(bytes));
        }
    }

    private void storeTimestamp(ZonedDateTime timestamp){
        File file = new File(getContext().getFilesDir(), contactsTimestampFileName);

        try {
            if (!file.exists())
                file.createNewFile();

            try (FileOutputStream fos = getContext().openFileOutput(contactsTimestampFileName, Context.MODE_PRIVATE)) {
                fos.write(timestamp.toString().getBytes());
            }
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "storeTimeStamp error", Toast.LENGTH_LONG).show();
        }
    }

    private List<Contact> loadContactsFromInternal(){
        File file = new File(getContext().getFilesDir(), contactsDataFileName);

        if (!file.exists())
            return new ArrayList<>();

        try(InputStreamReader isr = new InputStreamReader(getContext().openFileInput(contactsDataFileName))) {
            return gson.fromJson(isr, new TypeToken<List<Contact>>(){}.getType());
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "내부 저장소에서 연락처를 가져오는데 실패했습니다", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }
    }

    private void storeContactsToInternal(List<Contact> internalContacts){
        File file = new File(getContext().getFilesDir(), contactsDataFileName);

        try {
            if (!file.exists())
                file.createNewFile();

            try (OutputStreamWriter osw = new OutputStreamWriter(getContext().openFileOutput(contactsDataFileName, Context.MODE_PRIVATE))) {
                gson.toJson(internalContacts, new TypeToken<List<Contact>>() {
                }.getType(), osw);
            }
        }catch (IOException e){
            Toast.makeText(getContext(), "storeInternalContacts error", Toast.LENGTH_LONG).show();
        }
    }

    private void createContactToDBAsync(Contact contact, SyncFlag syncFlag){
        contactService.createContact(contact).enqueue(new Callback<MyResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if(response.isSuccessful()){
                    if(response.body().result != 1) {
                        Toast.makeText(getContext(), "createContactDBAsync: DB error", Toast.LENGTH_LONG).show();

                        if(contact.getTimestamp().isBefore(syncFlag.update))
                            syncFlag.update = contact.getTimestamp();
                    }
                }else {
                    Toast.makeText(getContext(), "createContactDBAsync: DB에 연락처를 업로드하는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();

                    if(contact.getTimestamp().isBefore(syncFlag.update))
                        syncFlag.update = contact.getTimestamp();
                }

                syncFlag.n--;
                if(syncFlag.n == 0){
                    timestamp = syncFlag.update;
                    storeTimestamp(timestamp);
                    storeContactsToInternal(internalContacts);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(getContext(), "createContactDBAsync: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();

                if(contact.getTimestamp().isBefore(syncFlag.update))
                    syncFlag.update = contact.getTimestamp();

                syncFlag.n--;
                if(syncFlag.n == 0){
                    timestamp = syncFlag.update;
                    storeTimestamp(timestamp);
                    storeContactsToInternal(internalContacts);
                }
            }
        });
    }

    private void updateContactToDBAsync(Contact contact, SyncFlag syncFlag){
        contactService.updateContact(contact.getUUID(), contact).enqueue(new Callback<MyResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if(response.isSuccessful()){
                    if(!"person updated".equals(response.body().message)) {
                        Toast.makeText(getContext(), "updateContactDBAsync: " + response.code() + "\n" + response.body().error, Toast.LENGTH_LONG).show();

                        if(contact.getTimestamp().isBefore(syncFlag.update))
                            syncFlag.update = contact.getTimestamp();
                    }
                }else{
                    Toast.makeText(getContext(), "updateContactDBAsync: " + response.code() + "\n" + response.body().error, Toast.LENGTH_LONG).show();

                    if(contact.getTimestamp().isBefore(syncFlag.update))
                        syncFlag.update = contact.getTimestamp();
                }

                syncFlag.n--;
                if(syncFlag.n == 0){
                    timestamp = syncFlag.update;
                    storeTimestamp(timestamp);
                    storeContactsToInternal(internalContacts);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(getContext(), "updateContactDBAsync: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();

                if(contact.getTimestamp().isBefore(syncFlag.update))
                    syncFlag.update = contact.getTimestamp();

                syncFlag.n--;
                if(syncFlag.n == 0){
                    timestamp = syncFlag.update;
                    storeTimestamp(timestamp);
                    storeContactsToInternal(internalContacts);
                }
            }
        });
    }

    public void deleteContactAsync(Contact contact){
        int index = internalContacts.indexOf(contact);

        contactService.deleteContact(internalContacts.get(index).getUUID()).enqueue(new Callback<MyResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if(!response.isSuccessful())
                    Toast.makeText(getContext(), "delete contact: 연락처를 DB에서 삭제하는데 실패했습니다\nHTTP status code:" + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(getContext(), "delete contact: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });

        internalContacts.remove(index);
        storeContactsToInternal(internalContacts);
    }

    private <T> T nextOrNull(Iterator<T> iterator){
        if(iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    // 동시성 문제 존재
    private List<Contact> syncContacts(List<Contact> internalContacts, List<Contact> dbContacts){
        boolean flag = true;
        ZonedDateTime now = ZonedDateTime.now();
        SyncFlag syncFlag = new SyncFlag(0, now);

        dbContacts.sort(null);

        Log.e("internalContacts", internalContacts.toString());
        Log.e("dbContacts", dbContacts.toString());

        ListIterator<Contact> internalIterator = internalContacts.listIterator();
        Iterator<Contact> dbIterator = dbContacts.iterator();

        Contact internalContact = nextOrNull(internalIterator);
        Contact dbContact = nextOrNull(dbIterator);

        while(internalContact != null && dbContact != null){
            int compare = internalContact.uuid.compareTo(dbContact.uuid);

            if(compare == 0){
                if(internalContact.getTimestamp().isAfter(dbContact.getTimestamp())){
                    if(!internalContact.getTimestamp().isBefore(timestamp)) {
                        flag = false;
                        syncFlag.n++;

                        updateContactToDBAsync(internalContact, syncFlag);
                    }
                } else {
                    if(!dbContact.getTimestamp().isBefore(timestamp))
                        internalIterator.set(dbContact);
                }

                internalContact = nextOrNull(internalIterator);
                dbContact = nextOrNull(dbIterator);
            } else if(compare < 0){
                if(!internalContact.getTimestamp().isBefore(timestamp)) {
                    flag = false;
                    syncFlag.n++;

                    createContactToDBAsync(internalContact, syncFlag);
                }

                internalContact = nextOrNull(internalIterator);
            } else {
                internalIterator.add(dbContact);

                dbContact = nextOrNull(dbIterator);
            }
        }

        while(internalContact != null){
            if(!internalContact.getTimestamp().isBefore(timestamp)) {
                flag = false;
                syncFlag.n++;

                createContactToDBAsync(internalContact, syncFlag);
            }

            internalContact = nextOrNull(internalIterator);
        }

        while(dbContact != null){
            internalIterator.add(dbContact);

            dbContact = nextOrNull(dbIterator);
        }

        if(flag){
            timestamp = now;
            storeTimestamp(timestamp);
            storeContactsToInternal(internalContacts);
        }

        return internalContacts;
    }
}