package com.utils.gdkcorp.albums.ApiInterfaces;

import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.RequestDTO.SubscriptionBody;
import com.utils.gdkcorp.albums.ResponseDTO.SubscriptionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public interface SubscriptionApiInterface {
    @Headers({
            "Content-Type: application/json",
            "Authorization: "+ Constants.FIREBASE_MESSAGING_SERVICE.AUTHORIZATION_KEY
    })
    @POST("https://iid.googleapis.com/iid/v1:batchAdd")
    public Call<SubscriptionResponse> subscribeToTrip(@Body SubscriptionBody body);
}
