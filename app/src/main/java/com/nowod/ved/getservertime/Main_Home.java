package com.nowod.ved.getservertime;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.nowod.ved.getservertime.service.RestApiService;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Main_Home extends AppCompatActivity {

    private final String TAG = "Main_Home";
    public Activity activity;

    private Retrofit mRetrofit;
    private RestApiService mGetServerTime;


    @BindView(R.id.searchBar) MaterialSearchBar searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        ButterKnife.bind(this);

        setInit();
        setEvent();
    }

    public void setInit(){
        activity = Main_Home.this;
    }

    public void setEvent() {
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {}

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Log.d("onSearchConfirmed", searchBar.getText().toString());
                call_server();
            }

            @Override
            public void onButtonClicked(int buttonCode) {}
        });
    }

    private void call_server() {
        Log.d("Call Server", "###Call Server###");
        mRetrofit = new Retrofit.Builder()
                .baseUrl(RestApiService.API_URL)
                 .addConverterFactory(GsonConverterFactory.create())
                .build();

        mGetServerTime = mRetrofit.create(RestApiService.class);

        Call<ResponseBody> getTime = mGetServerTime.get_server_time();
        getTime.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    long requestTime = response.raw().sentRequestAtMillis();
                    long responseTime = response.raw().receivedResponseAtMillis();
                    long apiTime = responseTime - requestTime;
                    Log.d("server_time", response.headers().get("date"));
                    Log.d("response_time", String.valueOf(apiTime));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("fail", "fail");
            }
        });
    }
}
