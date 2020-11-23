package com.bgham.smarteye;

/**
 * used when making calls to server
 * @Reference: https://developer.android.com/reference/android/os/StrictMode
 */
public class StrictModeClass {
    public static void StrictMode() {
        android.os.StrictMode.ThreadPolicy policy =
                new android.os.StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();

        android.os.StrictMode.setThreadPolicy(policy);
    }
}
