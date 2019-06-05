package com.nowod.ved.getservertime.service;

import com.nowod.ved.getservertime.data.ServerDO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RestApiService {
    @POST(".")
    Call<ResponseBody> get_server_time();
}
