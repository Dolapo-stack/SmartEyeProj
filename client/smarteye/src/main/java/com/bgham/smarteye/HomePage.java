package com.bgham.smarteye;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends WearableActivity {

    boolean isPanicMode = false; // by default panic status is "false"
    Button panicBtn;
    Button logOutBtn;
    ImageButton microphoneBtn;
    TextView statusValue;
    TextView txtSpeechValue;
    TextView txtWelcomeValue;

    // Persistent storage in the app
    // go to Preference.java for more
    public static Preference appPreference;

    // google speech recognizer input code
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // instantiate Preference class
        appPreference = new Preference(this);

        txtWelcomeValue = findViewById(R.id.textWelcome);
        statusValue = findViewById(R.id.textStatusValue);
        txtSpeechValue = findViewById(R.id.txtSpeechResp);
        panicBtn = findViewById(R.id.activatePanicBtn);
        logOutBtn = findViewById(R.id.logoutBtn);
        microphoneBtn = findViewById(R.id.micBtn);

        // when the app first runs, request Location permission from user
        ActivityCompat.requestPermissions(HomePage.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);

        // check Shared preference for panic status
        // default text and color of the button is "Activate Panic"
        if (appPreference.getPanicStatus().equals("PANIC")) {
            isPanicMode = true;
            statusValue.setTextColor(Color.RED);
            panicBtn.setBackgroundColor(Color.DKGRAY);
            panicBtn.setText("Disable Panic");
            statusValue.setText("Panic Mode");
        }else{
            statusValue.setTextColor(Color.GREEN);
            panicBtn.setBackgroundColor(Color.RED);
            panicBtn.setText("Activate Panic");
        }

        // get the user's full name from preference and display on screen
        txtWelcomeValue.setText(String.format("Hi, %s", appPreference.getFullName()));

        // click the "activate panic" button to initialize panic mode
        panicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPanicMode();
            }
        });

        // click the "microphone" button to activate voice recognizer
        microphoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginListen();
            }
        });

        // click the "log out" button to log the user out
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    /**
     *  call the Google speech recognizer library "RecognizerIntent" and process user's speech
     *  @Note: add RECORD_AUDIO permission in Manifest
     *  @Reference: https://developer.android.com/reference/android/speech/SpeechRecognizer
     */
    private void beginListen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, say a panic word.");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            // throw an exception if this android device doesn't support speech capability
            Toast.makeText(HomePage.this,
                    "Oops! Your device doesn't support Speech-to-Text", Toast.LENGTH_SHORT).show();
            Log.e("message", "Device doesn't support Speech-to-Text");
        }
    }

    /**
     * the Google library "RecognizerIntent" speech result is processed
     * and sent to the server to determine if the speech contains panic word(s) or not
     * we also show on the screen what the user said
     * @Note: this may have issues working in an emulator depending on mic hardware availability
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // get what the user said and show in an EditTextView
                    txtSpeechValue.setText(result.get(0));
                    // log transcribed result
                    Log.e("message", result.get(0));
                }
        }
    }

    /** with this method, we trigger getting the location of the user
     * by asking location permission from the user
     * The gps coordinates are computed in the 'GPS' class
     * And the Point class sends the exact coordinates to the server
     * if the status=active it sends new coordinates
     */
    private void checkMyLocation(String status) {
        MyLocation g = new MyLocation(this);
        android.location.Location location = g.getLocation();
        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String log_coords = "LATITUDE: "+latitude+" \n LONGITUDE: "+longitude;
            Log.e("message", log_coords);
            if(status.equals("active")) {
                sendLocationCoords(longitude, latitude);
            }else{
                cancelPanicMode(longitude, latitude);
            }
        }
    }

    /* send location data to server */
    public void sendLocationCoords(double lng, double lat) {
        Api api = Config.getInstance().create(Api.class);
        Call<Point> call = api.userCoords(appPreference.getUserId(), lng, lat); // hardcoded userid

        call.enqueue(new Callback<Point>() {
            @Override
            public void onResponse(Call<Point> call, Response<Point> response) {
                Log.e("message", "Location sent!");
                //Toast.makeText(HomePage.this, "Location sent!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Point> call, Throwable t) {
                Log.e("message", "Something went wrong");
                //Toast.makeText(HomePage.this, "Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* send location data to server */
    public void cancelPanicMode(double lng, double lat) {
        Api api = Config.getInstance().create(Api.class);
        Call<ApiResponse> call = api.cancelPanic(appPreference.getUserId(), lng, lat); // hardcoded

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Log.e("message", "Panic mode cancelled!");
                //Toast.makeText(HomePage.this, "Panic mode cancelled!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("message", "Something went wrong");
                //Toast.makeText(HomePage.this, "Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** this method initializes panic mode
     * by changing the status of the user
     * and sending SMS/emails to emergency contacts
     */
    private void initPanicMode() {
        String toast_title;
        // then change status of panic button
        if(!isPanicMode) {
            checkMyLocation("active");
            statusValue.setTextColor(Color.RED);
            panicBtn.setBackgroundColor(Color.DKGRAY);
            panicBtn.setText("Disable Panic");
            statusValue.setText("Panic Mode");
            isPanicMode = true; // change panic mode to True
            toast_title ="You are now in panic mode!";
            appPreference.setPanicStatus("PANIC"); // update shared preference
        } else {
            checkMyLocation("normal");
            statusValue.setTextColor(Color.GREEN);
            panicBtn.setBackgroundColor(Color.RED);
            panicBtn.setText("Activate Panic");
            statusValue.setText("Normal");
            isPanicMode = false; // change panic mode to False
            toast_title ="Panic mode successfully cancelled!";
            appPreference.setPanicStatus("NORMAL"); // update shared preference
        }
        Toast.makeText(this, toast_title, Toast.LENGTH_SHORT).show();
        Log.e("message", toast_title);
    }

    /**
     * method called to log the user out of the app
     * set Android appPreference values for loginstatus=false and fullname=""
     * then change the activity to MainActivity (log in intent)
     */
    public void logout() {
        appPreference.setLoginStatus(false);
        appPreference.setFullName("");
        appPreference.setPanicStatus("NORMAL");
        Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(this, MainActivity.class), 100);
        Log.e("message", "User logged out! Shared preference cleared.");
    }
}