package com.young.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.young.coolweather.db.City;
import com.young.coolweather.db.County;
import com.young.coolweather.db.HeWeatherBean;
import com.young.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    public static HeWeatherBean handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,HeWeatherBean.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean handlrProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray provinceList = new JSONArray(response);
                for(int i =0; i<provinceList.length();i++){
                    JSONObject object = provinceList.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceId(object.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray cityList = new JSONArray(response);
                for(int i=0; i<cityList.length();i++){
                    JSONObject object = cityList.getJSONObject(i);
                    City city = new City();
                    city.setCityName(object.getString("name"));
                    city.setCityId(object.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray countyList = new JSONArray(response);
                for(int i=0;i<countyList.length();i++){
                    JSONObject object = countyList.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(object.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(object.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
