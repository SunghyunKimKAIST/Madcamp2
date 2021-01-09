package com.example.madcamp1st.contacts;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Contact {
    public String uuid;
    public String name;
    public String number;
    public String timestamp;

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