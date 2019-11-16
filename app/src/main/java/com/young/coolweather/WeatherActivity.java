package com.young.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.young.coolweather.db.HeWeatherBean;
import com.young.coolweather.util.HttpUtil;
import com.young.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    TextView tvCounty;
    TextView tvWeather;
    ImageView ivBingPic;
    SwipeRefreshLayout swipeRefresh;
    DrawerLayout drawerLayout;
    SharedPreferences sharedPreferences;
    String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        tvCounty = findViewById(R.id.tv_county);
        tvWeather = findViewById(R.id.tv_weather);
        ivBingPic = findViewById(R.id.iv_bing_pic);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);

        tvCounty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather",null);
        String bingPic = sharedPreferences.getString("bing_pic",null);
        if(weatherString != null){
            HeWeatherBean weatherBean = Utility.handleWeatherResponse(weatherString);
            weatherId = weatherBean.getBasic().getId();
            showWeatherInfo(weatherBean);
        }else{
            weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String weatherString = sharedPreferences.getString("weather",null);
                HeWeatherBean weatherBean = Utility.handleWeatherResponse(weatherString);
                weatherId = weatherBean.getBasic().getId();
                requestWeather(weatherId);
            }
        });

        if(bingPic !=null){
            Glide.with(this).load(bingPic).into(ivBingPic);
        }else{
            loadBingPic();
        }
    }

    private void loadBingPic() {
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(address, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(WeatherActivity.this,"获取背景图片失败",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                if(responseText !=null){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic",responseText);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(responseText).into(ivBingPic);

                        }
                    });
                }else{
                    Toast.makeText(WeatherActivity.this,"获取背景图片失败",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void requestWeather(final String weatherId) {
        String url = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=18b06362269d4626ae124682ecf7d6d7";
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final HeWeatherBean weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null & "ok".equals(weather.getStatus())){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(HeWeatherBean weatherBean) {
        tvCounty.setText(weatherBean.getBasic().getParent_city());
        tvWeather.setText(weatherBean.getNow().getCond_txt());
    }
}
