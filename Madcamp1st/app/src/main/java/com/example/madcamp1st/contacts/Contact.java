package com.example.madcamp1st.contacts;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Contact implements Comparable<Contact>{
    public String uuid;
    public String name;
    public String number;
    public String timestamp;
    public String fid;

    public Contact(String name, String number, String fid){
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.number = number;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        this.fid = fid;
    }

    public UUID getUUID(){
        return UUID.fromString(uuid);
    }

    public ZonedDateTime getTimestamp(){
        return ZonedDateTime.parse(timestamp);
    }

    @NonNull
    @Override
    public String toString(){
        return "\n{\"uuid\": \"" + uuid
                + "\", \"name\": \"" + name
                + "\", \"number\": \"" + number
                + "\", \"timestamp\": \"" + timestamp + "\"}";
    }

    @Override
    public int compareTo(Contact other) {
        return uuid.compareTo(other.uuid);
    }
}