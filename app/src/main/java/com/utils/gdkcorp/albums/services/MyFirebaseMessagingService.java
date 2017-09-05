package com.utils.gdkcorp.albums.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.activities.MainActivity;
import com.utils.gdkcorp.albums.models.Trip;
import com.utils.gdkcorp.albums.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Toast.makeText(this,"Notification Received",Toast.LENGTH_LONG).show();
        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);
        final String creator_id,trip_id;
        try {
            creator_id = object.getString("creator_id");
            trip_id = object.getString("trip_id");
            mDataRef.child("users").child(creator_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    mDataRef.child("trips").child(trip_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            Intent notificationIntent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
                            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0,
                                    notificationIntent, 0);
                            Intent joinIntent = new Intent(MyFirebaseMessagingService.this, TripService.class);
                            joinIntent.setAction(Constants.ACTION.STARTFOREGROUNG_JOIN_ACTION);
                            joinIntent.putExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY,trip_id);
                            joinIntent.putExtra(Constants.SHARE_DATA_KEYS.TRIP_NAME_KEY,trip.getName());
                            joinIntent.putExtra(Constants.SHARE_DATA_KEYS.TRIP_LOCATION_KEY,trip.getLocation());
                            PendingIntent pendingIntent1 = PendingIntent.getService(MyFirebaseMessagingService.this, 0,
                                    joinIntent, 0);
                            Notification notification = new NotificationCompat.Builder(MyFirebaseMessagingService.this)
                                    .setContentTitle("Your are Added to trip "+ trip.getName()+" by "+user.getName())
                                    .setContentText(user.getName() + " has started trip "+trip.getName()+" to "+trip.getLocation()+
                                            ".\nPress Join button to join the trip")
                                    .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                                    .setContentIntent(pendingIntent)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setPriority(Notification.PRIORITY_MAX)
                                    .addAction(R.drawable.ic_stop_grey_500_24dp,
                                            "JOIN", pendingIntent1).build();
                            ((NotificationManager)MyFirebaseMessagingService.this.getSystemService(Context.NOTIFICATION_SERVICE))
                                    .notify(Constants.NOTIFICATION_ID.JOIN_TRIP_NOTIFICATION_ID,notification);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeletedMessages() {

    }
}
