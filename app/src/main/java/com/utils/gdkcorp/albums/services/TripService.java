package com.utils.gdkcorp.albums.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.utils.gdkcorp.albums.ApiInterfaces.SendNotificationApiInterface;
import com.utils.gdkcorp.albums.ApiInterfaces.SubscriptionApiInterface;
import com.utils.gdkcorp.albums.ApiInterfaces.UnSubscribeApiInterface;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.RequestDTO.DataBody;
import com.utils.gdkcorp.albums.RequestDTO.SendMessageBody;
import com.utils.gdkcorp.albums.RequestDTO.SubscriptionBody;
import com.utils.gdkcorp.albums.ResponseDTO.SendMessageResponse;
import com.utils.gdkcorp.albums.ResponseDTO.SubscriptionResponse;
import com.utils.gdkcorp.albums.activities.MainActivity;
import com.utils.gdkcorp.albums.models.User;
import com.utils.gdkcorp.albums.receivers.CamaraPhotoReceiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripService extends Service {
    private PhotosContentJob job;
    private DatabaseReference mDataRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String tripName;
    private String tripLocation;
    private ArrayList<User> mList;
    private Gson gson;
    private ArrayList<String> rTokenList;
    public TripService() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            job = new PhotosContentJob();
        }
        mDataRef = FirebaseDatabase.getInstance().getReference();
        gson = new Gson();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)){
            Bundle bundle = intent.getExtras();
            tripName = bundle.getString(Constants.SHARE_DATA_KEYS.TRIP_NAME_KEY);
            tripLocation = bundle.getString(Constants.SHARE_DATA_KEYS.TRIP_LOCATION_KEY);
            mList = bundle.getParcelableArrayList(Constants.SHARE_DATA_KEYS.SHARE_DATA_KEY);
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Intent previousIntent = new Intent(this, TripService.class);
            previousIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            PendingIntent stopIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Trip to "+tripLocation+" is started")
                    .setContentText("Enjoy the trip")
                    .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setPriority(Notification.PRIORITY_MAX)
                    .addAction(R.drawable.ic_stop_grey_500_24dp,
                            "stop", stopIntent).build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
            final DatabaseReference ref = mDataRef.child("trips").push();
            SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY,MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            Log.i("TripService",ref.getKey());
            editor.putString(Constants.TRIP.TRIP_ID_PREFERENCE_KEY,ref.getKey());
            editor.putString(Constants.TRIP.TRIP_NAME_PREFERENCE_KEY,tripName);
            editor.putString(Constants.TRIP.TRIP_LOCATION_PREFERENCE_KEY,tripLocation);
            editor.commit();
            ref.child("id").setValue(ref.getKey());
            ref.child("name").setValue(tripName);
            ref.child("location").setValue(tripLocation);
            ref.child("creator_id").setValue(mAuth.getCurrentUser().getUid());
            ref.child("is_running").setValue(true);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String mDate = dateFormat.format(date);
            ref.child("date").setValue(mDate);
            DatabaseReference ref2 = mDataRef.child("user_trips").child(mAuth.getCurrentUser().getUid());
            ref2.child(ref.getKey()).setValue(ref.getKey());
            DatabaseReference ref1 = ref.child("friends");
            ref2 = mDataRef.child("user_trips");
            SubscriptionBody subscriptionBody = new SubscriptionBody();
            rTokenList = new ArrayList<String>();
            ref1.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
            for(int i=0;i<mList.size();++i){
                User user = mList.get(i);
                ref1.child(user.getUser_id()).setValue(user.getUser_id());
                DatabaseReference ref3 = ref2.child(user.getUser_id());
                ref3.child(ref.getKey()).setValue(ref.getKey());
                rTokenList.add(user.getRegistration_token());
            }
            Log.i("TripService",""+rTokenList);
            subscriptionBody.setTo("/topics/"+ref.getKey());
            subscriptionBody.setTokens(rTokenList);
            Log.i("TripService","SubscriptionBody : "+gson.toJson(subscriptionBody) );
            SubscriptionApiInterface subscriptionApiService = ApiService.getInstance().create(SubscriptionApiInterface.class);
            Call<SubscriptionResponse> call = subscriptionApiService.subscribeToTrip(subscriptionBody);
            call.enqueue(new Callback<SubscriptionResponse>() {
                @Override
                public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> response) {
                    //Toast.makeText(TripService.this,"onResponse Subscription",Toast.LENGTH_SHORT);
                    Log.i("TripService","onResponse Subscription");
                    if(response.body()!=null) {
                        Log.i("TripService", response.body().toString());
                    }else{
                        Log.i("TripService", "response body null");
                    }
                    SendMessageBody sendMessageBody = new SendMessageBody();
                    sendMessageBody.setTo("/topics/"+ref.getKey());
                    sendMessageBody.setData(new DataBody(ref.getKey(),mAuth.getCurrentUser().getUid()));
                    SendNotificationApiInterface sendNotificationApiInterface = ApiService.getInstance().create(SendNotificationApiInterface.class);
                    Log.i("TripService","SendMessageBody : "+gson.toJson(sendMessageBody));
                    Call<SendMessageResponse> call1 = sendNotificationApiInterface.sendFCMMessage(sendMessageBody);
                    call1.enqueue(new Callback<SendMessageResponse>() {
                        @Override
                        public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
//                            Toast.makeText(TripService.this,response.body().toString(),Toast.LENGTH_SHORT);
                            if(response.body()!=null) {
                                Log.i("TripService", response.body().toString());
                            }else{
                                Log.i("TripService", "response1 body null");
                            }
                        }

                        @Override
                        public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                            Log.i("TripService","sendMessage Failure");
                        }
                    });
                }

                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {
                    Log.i("TripService","Subscription failure");
                }
            });
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                job.scheduleJob(this);
            }else{
                getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), CamaraPhotoReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
            }
        }else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)){
            SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY,MODE_PRIVATE);
            String trip_id = preferences.getString(Constants.TRIP.TRIP_ID_PREFERENCE_KEY,null);
            mDataRef.child("trips").child(trip_id).child("is_running").setValue(false);
            stopForeground(true);
            SubscriptionBody subscriptionBody = new SubscriptionBody();
            subscriptionBody.setTokens(rTokenList);
            subscriptionBody.setTo("/topics/"+trip_id);
            UnSubscribeApiInterface unSubscribeApiInterface = ApiService.getInstance().create(UnSubscribeApiInterface.class);
            Call<SubscriptionResponse> subscriptionResponseCall = unSubscribeApiInterface.unSubscribeToTrip(subscriptionBody);
            subscriptionResponseCall.enqueue(new Callback<SubscriptionResponse>() {
                @Override
                public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> response) {
                    if(response.body().toString()!=null){
                        Log.i("TripService","Unsubscribed");
                        stopSelf();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {

                }
            });
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                job.cancelJob(this);
                job.stopSelf();
            }else{
                getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), CamaraPhotoReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
            }
        }else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_JOINED_ACTION)){
            stopForeground(true);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                job.cancelJob(this);
                job.stopSelf();
            }else{
                getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), CamaraPhotoReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
            }
            stopSelf();
        }else if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUNG_JOIN_ACTION)){
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Constants.NOTIFICATION_ID.JOIN_TRIP_NOTIFICATION_ID);
            final String trip_name = intent.getStringExtra(Constants.SHARE_DATA_KEYS.TRIP_NAME_KEY);
            final String trip_id = intent.getStringExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY);
            final String trip_location = intent.getStringExtra(Constants.SHARE_DATA_KEYS.TRIP_LOCATION_KEY);
            final SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY,MODE_PRIVATE);
            mDataRef.child("trips").child(trip_id).child("is_running").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean is_running = dataSnapshot.getValue(Boolean.class);
                    if(is_running){
                        Log.i("TripService",""+is_running+","+dataSnapshot.getKey());
                        Intent notificationIntent = new Intent(TripService.this, MainActivity.class);
                        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(TripService.this, 0,
                                notificationIntent, 0);

                        Intent previousIntent = new Intent(TripService.this, TripService.class);
                        previousIntent.setAction(Constants.ACTION.STOPFOREGROUND_JOINED_ACTION);
                        PendingIntent stopIntent = PendingIntent.getService(TripService.this, 0,
                                previousIntent, 0);

                        Notification notification = new NotificationCompat.Builder(TripService.this)
                                .setContentTitle("You have joined trip "+trip_name+" to location "+trip_location)
                                .setContentText("Enjoy the trip")
                                .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                                .setContentIntent(pendingIntent)
                                .setOngoing(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setPriority(Notification.PRIORITY_MAX)
                                .addAction(R.drawable.ic_stop_grey_500_24dp,
                                        "stop", stopIntent).build();
                        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                                notification);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(Constants.TRIP.TRIP_ID_PREFERENCE_KEY,trip_id);
                        editor.putString(Constants.TRIP.TRIP_NAME_PREFERENCE_KEY,trip_name);
                        editor.putString(Constants.TRIP.TRIP_LOCATION_PREFERENCE_KEY,trip_location);
                        editor.commit();
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                            job.scheduleJob(TripService.this);
                        }else{
                            getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), CamaraPhotoReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
