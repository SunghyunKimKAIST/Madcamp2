package com.example.madcamp1st.contacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private List<Contact> filtered;

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public ImageView android;
        public TextView name;
        public TextView phone;
        public ImageView calling;

        public ContactViewHolder(View itemView) {
            super(itemView);
            android = itemView.findViewById(R.id.android);
            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
            calling = itemView.findViewById(R.id.calling);
        }
    }

    public ContactAdapter(List<Contact> contacts) {
        this.contacts = new ArrayList<>(contacts);
        this.contacts.sort((l, r) -> l.name.compareTo(r.name));
        filtered = this.contacts;
    }

    public void updateContacts(List<Contact> contacts) {
        this.contacts = new ArrayList<>(contacts);
        this.contacts.sort((l, r) -> l.name.compareTo(r.name));
        filtered = this.contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactAdapter.ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.textview_contacts, parent, false);

        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = filtered.get(position);

        holder.name.setText(contact.name);
        holder.phone.setText(contact.number);

        holder.calling.setOnClickListener(v -> {
            Context c = v.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("tel:" + contact.number));

            try {
                c.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    public void filter(String text) {
        if(text.isEmpty())
            filtered = contacts;
        else {
            filtered = new ArrayList<>();
            text = text.toLowerCase();
            for (Contact contact : contacts) {
                //match by name
                if (contact.name.toLowerCase().contains(text)) {
                    filtered.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }
}