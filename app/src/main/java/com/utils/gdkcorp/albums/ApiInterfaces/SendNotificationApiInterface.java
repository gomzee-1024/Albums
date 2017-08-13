package com.utils.gdkcorp.albums.ApiInterfaces;

import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.RequestDTO.SendMessageBody;
import com.utils.gdkcorp.albums.ResponseDTO.SendMessageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public interface SendNotificationApiInterface {
    @Headers({
            "Content-Type: application/json",
            "Authorization: "+ Constants.FIREBASE_MESSAGING_SERVICE.AUTHORIZATION_KEY
    })
    @POST("fcm/send")
    public Call<SendMessageResponse> sendFCMMessage(@Body SendMessageBody body);
}
