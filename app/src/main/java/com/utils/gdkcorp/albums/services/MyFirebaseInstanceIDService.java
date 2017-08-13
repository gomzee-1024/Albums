package com.utils.gdkcorp.albums.services;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    //FirebaseAuth.AuthStateListener mAuthSateListener;

    @Override
    public void onTokenRefresh() {
        final String refressToken = FirebaseInstanceId.getInstance().getToken();
        if(mAuth.getCurrentUser()!=null)
        mDataRef.child("users").child(mAuth.getCurrentUser().getUid()).child("registration_token").setValue(refressToken);
    }
}
