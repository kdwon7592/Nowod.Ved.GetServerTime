package com.nowod.ved.getservertime;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nowod.ved.getservertime.data.DataHelper;
import com.nowod.ved.getservertime.service.RestApiService;
import com.nowod.ved.getservertime.util.Global;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Request extends AppCompatActivity{

    // Activity Variable
    private final String TAG = "Request";
    private Activity activity;

    // Retrofit Variable
    private Retrofit mRetrofit;
    private RestApiService mRestApi;

    // ButterKnife Bind
    @BindView(R.id.submitRequest)   Button submitRequest;
    @BindView(R.id.backBtn)         ImageButton backBtn;
    @BindView(R.id.editTextName)    EditText editTextName;
    @BindView(R.id.editTextUrl)     EditText editTextUrl;
    @BindView(R.id.editTextRequest) EditText editTextRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        ButterKnife.bind(this);

        setInit();
        setEvent();
    }

    public void setInit(){
        activity = Request.this;
        DataHelper.sServerSuggestions.clear();
    }

    public void setEvent() {
    }

    @OnClick(R.id.backBtn)
    public void onClickBackBtn() {
        activity.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        activity.finish();
    }

    @OnClick(R.id.submitRequest)
    public void submit() {
        if (editTextName.getText().length() < 1) {
            Toast.makeText(activity, "학교 이름을 입력해주세요.", Toast.LENGTH_LONG).show();
        } else if (editTextUrl.getText().length() < 1) {
            Toast.makeText(activity, "요청 주소를 입력해주세요.", Toast.LENGTH_LONG).show();
        } else {
            insertRequest();
        }
    }

    private void insertRequest() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Global.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRestApi = mRetrofit.create(RestApiService.class);
        String name = editTextName.getText().toString();
        String url = editTextUrl.getText().toString();
        String request = editTextRequest.getText().toString();
        if (request.length() < 1) {
            request = "NONE";
        }
        Call<ResponseBody> insert_data = mRestApi.insert_request(name,url,request);

        insert_data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d("response", response.toString());
                    Toast.makeText(activity, "요청이 완료되었습니다.", Toast.LENGTH_LONG).show();
                    activity.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("fail", "fail");
                Toast.makeText(activity, "오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
}
