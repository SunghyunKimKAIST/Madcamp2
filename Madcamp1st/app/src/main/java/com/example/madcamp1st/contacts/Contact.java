package com.example.madcamp1st.contacts;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Contact {
    public String uuid;
    public String name;
    public String number;
    public String timestamp;

    public Contact(String name, String number){
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.number = number;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
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
}