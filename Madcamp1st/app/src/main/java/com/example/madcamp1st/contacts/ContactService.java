package com.example.madcamp1st.contacts;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ContactService {
    @GET("api/persons")
    Call<List<Contact>> getAllContacts();

    @GET("api/persons/newer/{time}")
    Call<List<Contact>> getAllNewerContacts(@Path("time") String timestamp);

    @POST("api/persons")
    Call<ContactResponse> createContact(@Body Contact contact);

    @PUT("api/persons/{uuid}")
    Call<ContactResponse> updateContact(@Path("uuid") UUID uuid, @Body Contact contact);
}