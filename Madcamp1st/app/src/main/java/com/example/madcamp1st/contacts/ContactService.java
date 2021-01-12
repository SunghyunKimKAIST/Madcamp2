package com.example.madcamp1st.contacts;

import com.example.madcamp1st.MyResponse;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ContactService {
    @GET("api/persons/newer/{fid}/{time}")
    Call<List<Contact>> getAllNewerContacts(@Path("fid")String fid, @Path("time") String timestamp);

    @POST("api/persons")
    Call<MyResponse> createContact(@Body Contact contact);

    @PUT("api/persons/{fid}/{uuid}")
    Call<MyResponse> updateContact(@Path("fid")String fid, @Path("uuid") UUID uuid, @Body Contact contact);

    @DELETE("api/persons/{fid}/{uuid}")
    Call<MyResponse> deleteContact(@Path("fid")String fid, @Path("uuid") UUID uuid);
}