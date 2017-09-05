package com.utils.gdkcorp.albums.AsynkTasks;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.utils.gdkcorp.albums.Constants;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gautam Kakadiya on 15-08-2017.
 */

public class CompressUploadImageTask extends AsyncTask<Integer,Void,Void> {
    Bitmap mBitmap;
    String mFileName;
    String original_url;
    int bitmap_type;
    private DatabaseReference mDataRef;
    private StorageReference mStoreRef;
    private FirebaseAuth mAuth;
    private String tripId;
    public CompressUploadImageTask(Bitmap bmp, String fileName, String original_url,int bitmap_type,String trip_id){
        mBitmap = bmp;
        mFileName = fileName;
        this.original_url = original_url;
        this.bitmap_type = bitmap_type;
        tripId = trip_id;
        mDataRef = FirebaseDatabase.getInstance().getReference().child("trips").child(tripId).child("pictures");
        mStoreRef = FirebaseStorage.getInstance().getReference().child("pictures");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        switch (integers[0]){
            case 0 :
                if(bitmap_type== Constants.IMAGES_TYPE.LANSCAPE_TYPE) {
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, Constants.IMAGES_DIMENSION_ASPECT.FOUR_TO_THREE_RATIO_WIDTH, Constants.IMAGES_DIMENSION_ASPECT.FOUR_TO_THREE_RATIO_HEIGHT, true);
                }else{
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, Constants.IMAGES_DIMENSION_ASPECT.FOUR_TO_THREE_RATIO_HEIGHT, Constants.IMAGES_DIMENSION_ASPECT.FOUR_TO_THREE_RATIO_WIDTH, true);
                }
                break;
            case 1 :
                if(bitmap_type==Constants.IMAGES_TYPE.LANSCAPE_TYPE) {
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, Constants.IMAGES_DIMENSION_ASPECT.SIXTEEN_TO_NINE_RATIO_WIDTH, Constants.IMAGES_DIMENSION_ASPECT.SIXTEEN_TO_NINE_RATIO_HEIGHT, true);
                }else {
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, Constants.IMAGES_DIMENSION_ASPECT.SIXTEEN_TO_NINE_RATIO_HEIGHT, Constants.IMAGES_DIMENSION_ASPECT.SIXTEEN_TO_NINE_RATIO_WIDTH, true);
                }
                break;
            case 2 :
                mBitmap = Bitmap.createScaledBitmap(mBitmap, Constants.IMAGES_DIMENSION_ASPECT.ONE_TO_ONE_RATIO_WIDTH, Constants.IMAGES_DIMENSION_ASPECT.ONE_TO_ONE_RATIO_HEIGHT,true);
                break;
            case 3 :
                mBitmap = Bitmap.createScaledBitmap(mBitmap,(int)(mBitmap.getWidth()*0.2),(int)(mBitmap.getHeight()*0.2),true);
                break;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mStoreRef.child(mFileName).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri uri = taskSnapshot.getDownloadUrl();
                DatabaseReference dataref = mDataRef.child(mFileName);
                dataref.child("picture_id").setValue(dataref.getKey());
                dataref.child("picture_url").setValue(uri.toString());
                dataref.child("taken_by").setValue(mAuth.getCurrentUser().getUid());
                dataref.child("original_uri").setValue(original_url);
            }
        });
        return null;
    }


}
