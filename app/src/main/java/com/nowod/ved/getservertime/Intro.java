package com.nowod.ved.getservertime;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nowod.ved.getservertime.data.DataHelper;
import com.nowod.ved.getservertime.data.ServerSuggestion;
import com.nowod.ved.getservertime.service.RestApiService;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.nowod.ved.getservertime.util.Global.SERVER_URL;

public class Intro extends AppCompatActivity {

    // Activity
    private final String TAG = "Intro";
    private Activity activity;
    private String currentVersion;
    private ArrayList<ServerSuggestion> serverSuggestions = new ArrayList<>();

    // Retrofit Variable
    private Retrofit mRetrofit;
    private RestApiService mRestApi;

    // FireBase
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        setInit();
    }

    public void setInit(){
        activity = Intro.this;
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        fetchRemoteConfig();
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
                        } else {
                            Toast.makeText(activity, "에러 발생",
                                    Toast.LENGTH_LONG).show();
                        }
                        try {
                            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                            String version = mFirebaseRemoteConfig.getString("server_time_version");
                            Log.d(TAG, "updateVersion: " + version);
                            if (version.compareTo(currentVersion) > 0) {
                                appUpdate();
                            } else {
                                Intent intent = new Intent(activity, Main_Home.class);
                                startActivity(intent);
                                activity.finish();
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
