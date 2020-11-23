package com.bgham.smarteye;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Android Shared Preference class contains methods for storing user data on device
 * and checking login status, panic status
 * @Reference: https://developer.android.com/training/data-storage/shared-preferences#java
 */
public class Preference {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public Preference(Context context){
        // Preference File is "Settings"
        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // getter & setter user id
    public void setUserId(int userId){
        editor.putInt("USER_ID", userId);
        editor.apply();
    }
    public int getUserId(){
        return sharedPreferences.getInt("USER_ID", 0);
    }

    // getter & setter login status
    public void setLoginStatus(boolean status){
        editor.putBoolean("LOGIN_STATUS", status);
        editor.apply();
    }
    public boolean getLoginStatus(){
        return sharedPreferences.getBoolean("LOGIN_STATUS", false);
    }

    // getter & setter user full name
    public void setFullName(String name){
        editor.putString("USER_FULL_NAME", name);
        editor.apply();
    }
    public String getFullName(){
        return sharedPreferences.getString("USER_FULL_NAME", "");
    }

    // getter & setter user panic status
    public void setPanicStatus(String status){
        editor.putString("USER_PANIC_STATUS", status);
        editor.apply();
    }
    public String getPanicStatus(){
        return sharedPreferences.getString("USER_PANIC_STATUS", "NORMAL");
    }

}