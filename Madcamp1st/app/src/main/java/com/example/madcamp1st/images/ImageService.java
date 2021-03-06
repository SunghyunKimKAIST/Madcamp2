package com.example.madcamp1st.images;

import com.example.madcamp1st.MyResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ImageService {
    @GET("api/images/{fid}")
    Call<List<Image>> getAllImageName(@Path("fid")String fid);

    @GET("api/images/{fid}/{filename}")
    Call<ResponseBody> downloadImage(@Path("fid")String fid, @Path("filename") String filename);

    @Multipart
    @POST("api/images")
    Call<ResponseBody> uploadImage(
            @Part("fid") RequestBody description,
            @Part MultipartBody.Part file
    );

    @DELETE("api/images/{fid}/{filename}")
    Call<MyResponse> deleteImage(@Path("filename") String filename);
}