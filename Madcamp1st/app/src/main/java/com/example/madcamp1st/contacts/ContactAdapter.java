package com.example.madcamp1st.contacts;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcamp1st.R;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private final Fragment_Contacts fragment_contacts;

    private List<Contact> contacts;
    private List<Contact> filtered;
    private String query = "";

    public class ContactViewHolder extends RecyclerView.ViewHolder {
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

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();

                if(position == RecyclerView.NO_POSITION)
                    return false;

                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                new MenuInflater(v.getContext()).inflate(R.menu.delete_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    if(item.getItemId() == R.id.delete){
                        Contact contact = filtered.get(position);

                        filtered.remove(position);
                        contacts.remove(contact);
                        notifyDataSetChanged();

                        fragment_contacts.deleteContactAsync(contact);

                        return true;
                    }else
                        return false;
                });

                popupMenu.show();

                return true;
            });
        }
    }

    public ContactAdapter(Fragment_Contacts fragment_contacts, List<Contact> contacts) {
        this.fragment_contacts = fragment_contacts;
        this.contacts = new ArrayList<>(contacts);
        this.contacts.sort((l, r) -> l.name.compareTo(r.name));
        filter("", false);
    }

    public void updateContacts(List<Contact> contacts) {
        this.contacts = new ArrayList<>(contacts);
        this.contacts.sort((l, r) -> l.name.compareTo(r.name));
        filter(query, true);
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

        holder.calling.setOnClickListener(v ->
            v.getContext().startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + contact.number)))
        );
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    public void filter(String text, boolean doNotifyDataSetChanged) {
        query = text;

        if(text.isEmpty())
            filtered = new ArrayList<>(contacts);
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

        if(doNotifyDataSetChanged)
            notifyDataSetChanged();
    }
}