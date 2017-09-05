package com.utils.gdkcorp.albums.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.utils.gdkcorp.albums.AsynkTasks.CompressUploadImageTask;
import com.utils.gdkcorp.albums.Constants;

import java.io.IOException;

/**
 * Created by Gautam Kakadiya on 14-08-2017.
 */

public class CamaraPhotoReceiver extends BroadcastReceiver {
    private DatabaseReference mDataRef;
    private StorageReference mStoreRef;
    private FirebaseAuth mAuth;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("CameraPhotoReceiver","inReceive "+ intent.getData().toString());
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY,Context.MODE_PRIVATE);
        String trip_id = preferences.getString(Constants.TRIP.TRIP_ID_PREFERENCE_KEY,null);
        mDataRef = FirebaseDatabase.getInstance().getReference().child("trips").child(trip_id).child("pictures");
        mStoreRef = FirebaseStorage.getInstance().getReference().child("pictures");
        mAuth = FirebaseAuth.getInstance();
        Uri imageUri = intent.getData();
        String path = getRealPathFromURI(context,intent.getData());
        String file_name_with_extention = Uri.parse(path).getLastPathSegment();
        String file_name = file_name_with_extention.substring(0,file_name_with_extention.length() - 4);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int original_height = bitmap.getHeight();
        int original_width = bitmap.getWidth();
        double aspect_ratio;
        int bitmap_type=0;
        if(original_width>=original_height) {
            aspect_ratio = ((double) original_width / original_height);
            bitmap_type = Constants.IMAGES_TYPE.LANSCAPE_TYPE;
        }else{
            aspect_ratio = ((double) original_height / original_width);
            bitmap_type = Constants.IMAGES_TYPE.PORTRAIT_TYPE;
        }
        CompressUploadImageTask task = new CompressUploadImageTask(bitmap,file_name,path,bitmap_type,trip_id);
        Log.i("PhotoContentJob","aspect_ratio "+aspect_ratio);
        if(aspect_ratio==Constants.IMAGES_ASPECT_RATIO.FOUR_TO_THREE_RATIO){
            Log.i("PhotoContentJob","case 0");
            task.execute(0);
        }else if(aspect_ratio==Constants.IMAGES_ASPECT_RATIO.SIXTEEN_TO_NINE_RATIO){
            Log.i("PhotoContentJob","case 1");
            task.execute(1);
        }else if(aspect_ratio==Constants.IMAGES_ASPECT_RATIO.ONE_TO_ONE_RATIO){
            Log.i("PhotoContentJob","case 2");
            task.execute(2);
        }else{
            Log.i("PhotoContentJob","case 3");
            task.execute(3);
        }

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
