package com.bgham.smarteye;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/** this is the class that manages requests sent
 * to API server, it uses retrofit2 call & response to
 * send and request data to the API
 */
public class LoginService {

    boolean isAuth = false; // the user is logged out bby default
    String fullName = ""; // users's full name is empty by default until log in to the app

    /** this method accepts phone number and password
     * and checks the server if the user is valid
     * It returns the user's full name if successful log in
     */
    public JSONObject checkUser(String Phone, String Password) throws IOException, JSONException {
        // "Api" java interface contains API routes for server
        Api api = Config.getInstance().create(Api.class);
        Call<ApiResponse> usersCall = api.loginUser(Phone, Password);

        Response<ApiResponse> loginResponse = usersCall.execute();
        if (loginResponse.isSuccessful() &&
            loginResponse.body().getStatus().equals("SUCCESS")) {
                JSONObject responseData = new JSONObject(new Gson().toJson(loginResponse.body()));
                Config.token += loginResponse.body().getToken(); // extra Retrofit authorization
                isAuth = true;
                fullName = loginResponse.body().getUserName(); // get user's full name from the server
                Log.e("message", responseData.toString());
                Log.e("message", "User log in: " + fullName);
             return responseData;
        }else {
            return null;
        }
    }
}



