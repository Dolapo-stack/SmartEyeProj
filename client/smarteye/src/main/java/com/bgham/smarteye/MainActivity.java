package com.bgham.smarteye;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends WearableActivity {

    EditText fieldPhoneNo,fieldPassword;
    Button btnWearLogin;

    // Persistent storage in the app
    // go to Preference.java for more
    public static Preference appPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate Preference class
        appPreference = new Preference(this);

        // when the app loads, check login status from sharedPreference
        // if true, go to Home page, else user should log in
        if (appPreference.getLoginStatus()){
            startActivityForResult(new Intent(this, HomePage.class), 100);
        }

        /* get the unique id of the design UI elements from xml */
        fieldPhoneNo = findViewById(R.id.loginUsername);
        fieldPassword = findViewById(R.id.loginPassword);
        btnWearLogin = findViewById(R.id.loginBtn);

        btnWearLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    login();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // Enables Always-on for smartwatch device
        setAmbientEnabled();
    }

    /* This method sends the request to our API server
     * to authenticate the user if they exist
     * The 'LoginService' class manages request sent to external servers
     */
    private void login() throws IOException, JSONException {
        // check login fields are not empty
        if(TextUtils.isEmpty(fieldPhoneNo.getText())){
            fieldPhoneNo.setError("Enter Phone number");
        }
        else if(TextUtils.isEmpty(fieldPassword.getText())){
            fieldPassword.setError("Enter Password");
        }
        else{
            String Phone = fieldPhoneNo.getText().toString();
            String Password = fieldPassword.getText().toString();

            LoginService loginSvc = new LoginService();
            StrictModeClass.StrictMode();
            // return a Json object
            // sample: {"id":"5","name":"","panic_status":"PANIC","status":"SUCCESS","token":"111"}
            JSONObject loginData = loginSvc.checkUser(Phone, Password);
            try {
                // check if json data contains user id if not return an error "user not logged in"
                if (loginData != null && loginData.has("id")) {
                    // log the user in, change login status
                    // and store their user id, full name in shared preference
                    appPreference.setUserId(loginData.getInt("id"));
                    appPreference.setFullName(loginData.getString("name"));
                    appPreference.setLoginStatus(true);
                    // get current user's panic status from server
                    appPreference.setPanicStatus(loginData.getString("panic_status"));
                    Toast.makeText(this, "Successful log in", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(this, HomePage.class), 100);
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect phone number or password.", Toast.LENGTH_SHORT).show();
                    Log.e("message", "Incorrect phone or password");
                    fieldPhoneNo.requestFocus();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
