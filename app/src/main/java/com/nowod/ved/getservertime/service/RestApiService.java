package com.nowod.ved.getservertime.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.nowod.ved.getservertime.util.Global.INSERT_DATA;
import static com.nowod.ved.getservertime.util.Global.SELECT_DATA;
import static com.nowod.ved.getservertime.util.Global.SELECT_URL;

public interface RestApiService {
    @POST(".")
    Call<ResponseBody> get_server_time();

    @POST(INSERT_DATA)
    Call<ResponseBody> insert_data(
            @Query("keyword") String keyword
    );

    @GET(SELECT_DATA)
    Call<ResponseBody> select_data();

    @GET(SELECT_URL)
    Call<ResponseBody> select_url(
            @Query("keyword") String keyword
    );
}
