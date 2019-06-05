package com.nowod.ved.getservertime;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.nowod.ved.getservertime.service.RestApiService;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Main_Home extends AppCompatActivity {

    // Activity Variable
    private final String TAG = "Main_Home";
    private Activity activity;
    private String currentVersion;

    public Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public long totalTime;
    public Timer timer;
    public TimerTask task;

    // Retrofit Variable
    private Retrofit mRetrofit;
    private RestApiService mGetServerTime;

    // Search Keyword
    public String keyword;

    // FireBase
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    // ButterKnife Bind
    @BindView(R.id.searchBar)       MaterialSearchBar searchBar;
    @BindView(R.id.tvServerTime)    TextView tvServerTime;
    @BindView(R.id.tvServerUrl)     TextView tvServerUrl;

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
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        fetchRemoteConfig();
    }

    public void setEvent() {
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {}

            @Override
            public void onSearchConfirmed(CharSequence text) {
                tvServerTime.setText("");
                keyword = searchBar.getText();
                call_server();
            }

            @Override
            public void onButtonClicked(int buttonCode) {}
        });
    }


    //Retrofit Rest API
    private void call_server() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(keyword)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mGetServerTime = mRetrofit.create(RestApiService.class);

        Call<ResponseBody> getTime = mGetServerTime.get_server_time();
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
                    tvServerTime.setText(format.format(totalTime));
                    tvServerUrl.setText(keyword + "의 현재 시간");
                    startTimer();

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

    public long headerDateToTime(String serverTime) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz");
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


    private void fetchRemoteConfig() {
        // [START fetch_config_with_callback]
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            Toast.makeText(Main_Home.this, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Main_Home.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        try {
                            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                            String version = mFirebaseRemoteConfig.getString("server_time_version");
                            Log.d(TAG, "updateVersion: " + version);
                            if (version.compareTo(currentVersion) > 0) {
                                appUpdate();
                            }
                        }catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
        // [END fetch_config_with_callback]
    }


    private void appUpdate() {
        if (!isFinishing()) {
            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(activity)
                    .setTitle("업데이트가 필요합니다.")
                    .setMessage("구글플레이스토어로 이동하여 업데이트를 하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            android.support.v7.app.AlertDialog dialog = alert.create();
            dialog.show();
        }
    }
}
