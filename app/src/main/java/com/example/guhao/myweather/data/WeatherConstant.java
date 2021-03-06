package com.example.guhao.myweather.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import com.example.guhao.myweather.ui.adapter.CityFragmentPagerAdapter;
import com.example.guhao.myweather.ui.adapter.MyPageScrollListener;
import com.example.guhao.myweather.data.bean.WeatherEntity;
import com.example.guhao.myweather.ui.fragment.SingleCityFragment;
import com.example.guhao.myweather.data.network.SubscriberOnNextListener;
import com.example.guhao.myweather.data.presenter.WeatherPre;
import com.example.guhao.myweather.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: GuHao
 * Date: 6/21/17
 * Time: 4:35 PM
 * Desc:
 */

public class WeatherConstant{
    public static List<WeatherEntity> weatherList = new ArrayList<>();
    public static List<String> citySlotList = new ArrayList<>();
    public static List<TextView> cardList = new ArrayList<>();
    private static SubscriberOnNextListener listener;
    private static final String TAG = "";

    public static void addLocal(String city, Context context){
        if (citySlotList.size() == 0){
            addCitySlot(city,context);
        }else{
            citySlotList.set(0,city);
        }
    }

    public static void addLocalEntity(WeatherEntity entity){
        if (weatherList.size() == 0){
            addWeatherEntity(entity);
        }else{
            weatherList.set(0,entity);
        }
    }

    public static void addCitySlot(String city, Context context){
        citySlotList.add(city);
        SharedPreferences.Editor editor = context.getSharedPreferences("city",Context.MODE_PRIVATE).edit();
        String key = "city" + (citySlotList.size()-1);
        editor.putString(key, city);
        editor.putInt("size", citySlotList.size());
        editor.apply();

    }

    public static void updateSharedPreferences(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("city",Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
        String key = "city";
        for (int i = 0; i < weatherList.size();i++){
            editor.putString(key+i, weatherList.get(i).getHeWeather5().get(0).getBasic().getCity());
            editor.commit();
        }

    }

    public static void updateLocal(String city, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("city",Context.MODE_PRIVATE).edit();
        editor.putString("city0",city);
        editor.commit();
    }

    public static void addWeatherEntity(WeatherEntity entity){
        if (!checkIfExist(entity)){
            weatherList.add(entity);
        }
    }

    public static boolean checkIfExist(WeatherEntity entity){
        for (int i = 0; i < weatherList.size(); i++){
            if (weatherList.get(i).getHeWeather5().get(0).getBasic().getId().equals(entity.getHeWeather5().get(0).getBasic().getId())){
                return true;
            }
        }
        return false;
    }


    public static void updateWeather(final int position, final CityFragmentPagerAdapter adapter, final SwipeRefreshLayout layout){
        listener = new SubscriberOnNextListener<WeatherEntity>(){
            @Override
            public void onNext(WeatherEntity entity) {
                weatherList.set(position,entity);
                adapter.updateFragment(position,getSingleCityFragment(entity, layout),entity);
//                adapter.addFragment(getSingleCityFragment(entity));

            }
        };
        WeatherPre.getWeatherRequest(citySlotList.get(position),listener);

    }

    public static void updateRawWeather(final SwipeRefreshLayout layout, CityFragmentPagerAdapter adapter){
        for (int i = 1; i < citySlotList.size(); i++) {
            updateSingleCity(i,layout, adapter);
        }
    }

    public static void updateSingleCity(final int i,final SwipeRefreshLayout layout, final CityFragmentPagerAdapter adapter){
        SubscriberOnNextListener<WeatherEntity> updateListener = new SubscriberOnNextListener<WeatherEntity>() {
            @Override
            public void onNext(WeatherEntity weatherEntity) {
                Log.d(TAG, "updateRawWeather: " + i);
                weatherList.set(i, weatherEntity);
                Log.d(TAG, "onNext: " + weatherEntity.toString());

                adapter.updateFragment(i,getSingleCityFragment(weatherEntity,layout),weatherEntity);
                layout.setRefreshing(false);
            }
        };

        WeatherPre.getWeatherRequest(weatherList.get(i).toString(), updateListener);
    }


    public static SingleCityFragment getSingleCityFragment(WeatherEntity entity, final SwipeRefreshLayout layout) {
        Bundle args = new Bundle();
        args = StringUtil.makeArgs(args,entity);
        SingleCityFragment cityFragment = new SingleCityFragment();
        cityFragment.setOnMyPageScrollListener(new MyPageScrollListener() {
            @Override
            public void setRefresh(boolean set) {
                if (layout != null){
                    layout.setEnabled(set);
                }
            }
        });
        cityFragment.setArguments(args);
        return cityFragment;
    }



}
