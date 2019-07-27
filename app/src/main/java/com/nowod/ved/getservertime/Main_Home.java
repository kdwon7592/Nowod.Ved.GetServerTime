package com.nowod.ved.getservertime;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.nowod.ved.getservertime.data.ServerSuggestion;
import com.nowod.ved.getservertime.data.DataHelper;
import com.nowod.ved.getservertime.service.RestApiService;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.nowod.ved.getservertime.util.Global.SERVER_URL;

public class Main_Home extends AppCompatActivity {

    // Activity Variable
    private final String TAG = "Main_Home";
    private Activity activity;

    private Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long totalTime;
    private Timer timer;
    private TimerTask task;
    private String timeFirst;
    private boolean isUrlSearch = false;

    // Retrofit Variable
    private Retrofit mRetrofit;
    private RestApiService mRestApi;

    // Search Keyword
    private String keyword;
    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;

    // ButterKnife Bind
    @BindView(R.id.searchBar)       FloatingSearchView searchBar;
    @BindView(R.id.tvServerTime)    TextView tvServerTime;
    @BindView(R.id.tvServerUrl)     TextView tvServerUrl;
    @BindView(R.id.tvServerName)    TextView tvServerName;
    @BindView(R.id.inputChangeBtn)  Button inputChangeBtn;
    @BindView(R.id.requestBtn)      Button requestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        ButterKnife.bind(this);

        setInit();
        setEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInit();
        setEvent();
    }


    public void setInit(){
        activity = Main_Home.this;
        DataHelper.sServerSuggestions.clear();
        getServerData();
    }

    public void setEvent() {
        searchBar.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                tvServerTime.setText("");
                keyword = searchSuggestion.getBody();
                searchBar.setSearchText(keyword);
                insertHistory();
                searchBar.clearFocus();
                closeSuggestion();
            }

            @Override
            public void onSearchAction(String currentQuery) {
                tvServerTime.setText("");
                keyword = searchBar.getQuery();
                insertHistory();
                searchBar.clearFocus();
                closeSuggestion();
            }
        });

        searchBar.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchBar.clearSuggestions();
                } else {
                    searchBar.showProgress();
                    DataHelper.findSuggestions(activity, newQuery, 3,
                                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

                                                @Override
                                                public void onResults(List<ServerSuggestion> results) {
                                                    searchBar.swapSuggestions(results);
                                                    searchBar.hideProgress();
                                }
                            });
                }
                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        searchBar.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                if(searchBar.getQuery().length() > 0){
                    DataHelper.findSuggestions(activity, searchBar.getQuery(), 3,
                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

                                @Override
                                public void onResults(List<ServerSuggestion> results) {
                                    searchBar.swapSuggestions(results);
                                    searchBar.hideProgress();
                                }
                            });
                } else {
                    if(!isUrlSearch) {
                        searchBar.swapSuggestions(DataHelper.getHistory(activity, 3));
                    }
                }
            }

            @Override
            public void onFocusCleared() {
            }
        });
    }

    @Override
    public void onBackPressed() {
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(activity)
                .setTitle("종료")
                .setMessage("종료 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        android.support.v7.app.AlertDialog dialog = alert.create();
        dialog.show();
    }

    @OnClick(R.id.inputChangeBtn)
    public void onClick() {
        if(!isUrlSearch) {
            searchBar.setSearchHint("주소를 입력해주세요. 예)http://test.com");
            searchBar.clearQuery();
            inputChangeBtn.setText("학교 검색하기");
            isUrlSearch = true;
        } else {
            searchBar.setSearchHint("학교이름을 입력해주세요.");
            searchBar.clearQuery();
            inputChangeBtn.setText("URL 직접 입력하기");
            isUrlSearch = false;
        }
    }

    @OnClick(R.id.requestBtn)
    public void onClickRequest() {
        Intent intent = new Intent(activity, Request.class);
        startActivity(intent);
    }

    private void getServerData(){
        mRetrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRestApi = mRetrofit.create(RestApiService.class);

        Call<ResponseBody> select_data = mRestApi.select_data();

        select_data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    Log.d("body", body);
                    String[] serverData = body.split(",");
                    Log.d("severData[0]", serverData[0]);

                    for(int i = 0 ; i <= serverData.length; i++){
                        DataHelper.sServerSuggestions.add(new ServerSuggestion(serverData[i]));
                        Log.d("serverData", DataHelper.sServerSuggestions.get(i).getBody());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("fail", "fail");
                Log.d("exception", t.getMessage());
            }
        });
    }

    private void insertHistory() {
        timeFirst = "";
        mRetrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRestApi = mRetrofit.create(RestApiService.class);

        Call<ResponseBody> insert_data = mRestApi.insert_data(keyword);

        insert_data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d("response", response.toString());
                    if(isUrlSearch){
                        callServerURL(keyword);
                    } else {
                        getServerUrl();
                    }
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

    private void getServerUrl(){
        mRetrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRestApi = mRetrofit.create(RestApiService.class);

        Call<ResponseBody> select_url = mRestApi.select_url(keyword);

        select_url.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String url = response.body().string();
                    Log.d("response", url);
                    tvServerUrl.setText(url);
                    callServerHttp(url);
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

    //Retrofit Rest API
    private void callServerURL(final String url) {
        try {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mRestApi = mRetrofit.create(RestApiService.class);

            Call<ResponseBody> getTime = mRestApi.get_server_time();
            getTime.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        // Get Response time
                        long startTime = new Date().getTime();
                        long requestTime = response.raw().sentRequestAtMillis();
                        long responseTime = response.raw().receivedResponseAtMillis();
                        long apiTime = responseTime - requestTime;
                        long serverTime = headerDateToTime(response.headers().get("date"));
                        long endTime = new Date().getTime();
                        totalTime = serverTime + apiTime + (startTime - endTime);
                        if(!checkTime(totalTime)) {
                            tvServerTime.setText(format.format(totalTime));
                            tvServerUrl.setText(keyword + "의 현재 시간");
                            tvServerName.setText("");
                            startTimer();
                        } else {
                            callServerURL(url);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "URL입력이 잘못되었습니다.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("fail", "fail");
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "URL입력이 잘못되었습니다.", Toast.LENGTH_LONG).show();
        }
    }

    //Retrofit Rest API
    private void callServerHttp(final String url) {
        try {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl("http://" + url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mRestApi = mRetrofit.create(RestApiService.class);

            Call<ResponseBody> getTime = mRestApi.get_server_time();
            getTime.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        // Get Response time
                        long startTime = new Date().getTime();
                        long requestTime = response.raw().sentRequestAtMillis();
                        long responseTime = response.raw().receivedResponseAtMillis();
                        long apiTime = responseTime - requestTime;
                        long serverTime = headerDateToTime(response.headers().get("date"));
                        long endTime = new Date().getTime();
                        totalTime = serverTime + apiTime + (startTime - endTime);
                        if(!checkTime(totalTime)) {
                            tvServerTime.setText(format.format(totalTime));
                            tvServerName.setText(keyword + "의 현재 시간");
                            startTimer();
                        } else {
                            callServerHttp(url);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "지원되지 않는 학교입니다.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("fail", "fail");
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
            callServerHttps(url);
        }
    }

    //Retrofit Rest API
    private void callServerHttps(final String url) {
        try {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl("https://" + url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mRestApi = mRetrofit.create(RestApiService.class);

            Call<ResponseBody> getTime = mRestApi.get_server_time();
            getTime.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        // Get Response time
                        long startTime = new Date().getTime();
                        long requestTime = response.raw().sentRequestAtMillis();
                        long responseTime = response.raw().receivedResponseAtMillis();
                        long apiTime = responseTime - requestTime;
                        long serverTime = headerDateToTime(response.headers().get("date"));
                        long endTime = new Date().getTime();
                        totalTime = serverTime + apiTime + (startTime - endTime);
                        if(!checkTime(totalTime)) {
                            tvServerTime.setText(format.format(totalTime));
                            tvServerName.setText(keyword + "의 현재 시간");
                            startTimer();
                        } else {
                            callServerHttps(url);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "지원되지 않는 학교입니다.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("fail", "fail");
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "지원되지 않는 학교입니다.", Toast.LENGTH_LONG).show();
        }
    }

    public long headerDateToTime(String serverTime) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz",  Locale.ENGLISH);
        Date date = dateFormat.parse(serverTime);
        return date.getTime();
    }

    public void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                totalTime += 1000;
                tvServerTime.setText(format.format(totalTime));
            }
        };

        timer.schedule(task, 1000, 1000);
    }

    public boolean checkTime(long totalTime) {
        String tempTime = format.format(totalTime);
        if(timeFirst.equals("")) {
            timeFirst = tempTime;
        } else if (timeFirst.equals(tempTime)) {
            return true;
        } else {
            return false;
        }
        return true;
    }

    public void closeSuggestion() {
        DataHelper.findSuggestions(activity,"", 0, FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

            @Override
            public void onResults(List<ServerSuggestion> results) {
                searchBar.swapSuggestions(results);
                searchBar.hideProgress();
            }
        });
    }
}
