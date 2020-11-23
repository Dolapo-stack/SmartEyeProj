package com.bgham.smarteye;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Config {

    // recommended android emulator url, thanks to Dr. SUPERVISOR_NAME
    public static final String base_url = "http://10.0.2.2:5000/api/";
    //public static final String base_url = "http://192.168.43.245:5000/api/";
    public static String token = "Bearer ";
    public static String imagePath = base_url + "images/" ;

    public static Retrofit getInstance() {
        return new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

}
