package com.bgham.smarteye;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @author Oluwaseun Akindolire
 */
public interface Api {

    /* log in route */
    @FormUrlEncoded
    @POST("login")
    Call<ApiResponse> loginUser(@Field("phone_no") String Phone, @Field("password") String Password);


    /** Activate panic
     * and send user unique id and coordinates data
     */
    @FormUrlEncoded
    @POST("panic/activate")
    Call<Point> userCoords(@Field("user_id") int UserId, @Field("long") double Lng, @Field("lat") double Lat);

    /* Deactivate panic */
    @FormUrlEncoded
    @POST("panic/cancel_panic")
    Call<ApiResponse> cancelPanic(@Field("user_id") int UserId, @Field("long") double Lng, @Field("lat") double Lat);
}
