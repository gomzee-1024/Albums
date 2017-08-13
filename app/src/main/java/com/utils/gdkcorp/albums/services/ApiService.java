package com.utils.gdkcorp.albums.services;

import com.utils.gdkcorp.albums.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class ApiService {
    private static Retrofit retrofit = null;

    public static Retrofit getInstance() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.FIREBASE_MESSAGING_SERVICE.BASE_URL_NOTIFICATION_API)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
