package com.example.madcamp1st.contacts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.example.madcamp1st.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
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

    private Gson gson;

    private ZonedDateTime timestamp;
    private final String contactsTimestampFileName = "contacts_timestamp";
    private final ZonedDateTime MIN_ZONED_DATE_TIME = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

    private List<Contact> internalContacts;
    private final String contactsDataFileName = "contacts_data";

    private final String DB_URL = "http://192.249.18.163:1234/";
    private ContactService contactService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gson = new Gson();

        // get cashed data
        try {
            timestamp = loadTimestamp();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "내부 저장소에서 timestamp를 불러오는데 실패했습니다", Toast.LENGTH_LONG).show();
            timestamp = MIN_ZONED_DATE_TIME;
        }

        try{
            internalContacts = loadInternalContacts();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "내부 저장소에서 연락처를 불러오는데 실패했습니다", Toast.LENGTH_LONG).show();
            internalContacts = new ArrayList<>();
        }

        // recyclerview
        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactAdapter mAdapter = new ContactAdapter(internalContacts);
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

                    try {
                        internalContacts = syncContacts(internalContacts, dbContacts);
                        mAdapter.updateContacts(internalContacts);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "syncContacts: 연락처를 DB와 동기화하는데 실패했습니다", Toast.LENGTH_LONG).show();
                    }
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
                mAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });

        // TODO: 연락처 추가 기능 (floating action button을 이용해서?)
    }

    private ZonedDateTime loadTimestamp() throws IOException {
        File file = new File(getContext().getFilesDir(), contactsTimestampFileName);

        if (!file.exists()) {
            Log.e("0", contactsTimestampFileName + " does not exists");
            return MIN_ZONED_DATE_TIME;
        }
        else
            return ZonedDateTime.parse(new String(Files.readAllBytes(file.toPath())));
    }

    private void storeTimestamp(ZonedDateTime timestamp) throws IOException{
        File file = new File(getContext().getFilesDir(), contactsTimestampFileName);

        if(!file.exists())
            file.createNewFile();

        try(FileOutputStream fos = getContext().openFileOutput(contactsTimestampFileName, Context.MODE_PRIVATE)){
            fos.write(timestamp.toString().getBytes());
        }
    }

    private List<Contact> loadInternalContacts() throws IOException {
        File file = new File(getContext().getFilesDir(), contactsDataFileName);

        if (!file.exists()) {
            Log.e("0", contactsDataFileName + "does not exists");
            return new ArrayList<>();
        }

        try(InputStreamReader isr = new InputStreamReader(getContext().openFileInput(contactsDataFileName))) {
            return gson.fromJson(isr, new TypeToken<List<Contact>>(){}.getType());
        }
    }

    private void storeInternalContacts(List<Contact> internalContacts) throws IOException {
        File file = new File(getContext().getFilesDir(), contactsDataFileName);

        if(!file.exists())
            file.createNewFile();

        try(OutputStreamWriter osw = new OutputStreamWriter(getContext().openFileOutput(contactsDataFileName, Context.MODE_PRIVATE))){
            gson.toJson(internalContacts, new TypeToken<List<Contact>>(){}.getType(), osw);
        }
    }

    private void createContactDBAsync(Contact contact){
        contactService.createContact(contact).enqueue(new Callback<ContactResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if(response.isSuccessful()){
                    if(response.body().result != 1)
                        Toast.makeText(getContext(), "createContactDBAsync: DB error", Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(getContext(), "createContactDBAsync: DB에 연락처를 업로드하는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                Toast.makeText(getContext(), "createContactDBAsync: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateContactDBAsync(Contact contact){
        contactService.updateContact(contact.getUUID(), contact).enqueue(new Callback<ContactResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if(response.isSuccessful()){
                    if(!"person updated".equals(response.body().message))
                        Toast.makeText(getContext(), "updateContactDBAsync: " + response.code() + "\n" + response.body().error, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getContext(), "updateContactDBAsync: " + response.code() + "\n" + response.body().error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                Toast.makeText(getContext(), "updateContactDBAsync: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    private <T> T nextOrNull(Iterator<T> iterator){
        if(iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    private List<Contact> syncContacts(List<Contact> internalContacts, List<Contact> dbContacts) throws IOException {
        dbContacts.sort((l, r) -> l.uuid.compareTo(r.uuid));

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
                    if(internalContact.getTimestamp().isAfter(timestamp))
                        updateContactDBAsync(internalContact);
                } else {
                    if(dbContact.getTimestamp().isAfter(timestamp))
                        internalIterator.set(dbContact);
                }

                internalContact = nextOrNull(internalIterator);
                dbContact = nextOrNull(dbIterator);
            } else if(compare < 0){
                if(internalContact.getTimestamp().isAfter(timestamp))
                    createContactDBAsync(internalContact);

                internalContact = nextOrNull(internalIterator);
            } else {
                internalIterator.add(dbContact);

                dbContact = nextOrNull(dbIterator);
            }
        }

        while(internalContact != null){
            if(internalContact.getTimestamp().isAfter(timestamp))
                createContactDBAsync(internalContact);

            internalContact = nextOrNull(internalIterator);
        }

        while(dbContact != null){
            internalIterator.add(dbContact);

            dbContact = nextOrNull(dbIterator);
        }

        timestamp = ZonedDateTime.now();
        storeTimestamp(timestamp);
        storeInternalContacts(internalContacts);

        return internalContacts;
    }
}