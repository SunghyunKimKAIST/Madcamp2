package com.example.madcamp1st.diary;

import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DiaryService {
    @GET("api/pages")
    Call<List<Page>> getAllPages();

    @GET("api/pages/{fid}/{date}")
    Call<Page> getPage(@Path("fid") String fid, @Path("date") String date);

    @GET("api/pages/average/{fid}")
    Call<ResponseBody> getAverageRating(@Path("fid") String fid);

    @POST("api/pages")
    Call<ResponseBody> createPage(@Body Page page);

    @PUT("api/pages/{fid}/{date}")
    Call<ResponseBody> updatePage(@Path("fid") String fid, @Path("date") Date date, @Body Page page);

    @DELETE("api/pages/{fid}/{date}")
    Call<ResponseBody> deletePage(@Path("fid") String fid, @Path("date") Date date);
}