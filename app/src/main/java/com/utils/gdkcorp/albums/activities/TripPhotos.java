package com.utils.gdkcorp.albums.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.GlideApp;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.Photo;
import com.utils.gdkcorp.albums.models.Trip;

import org.w3c.dom.Text;

public class TripPhotos extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Photo,TripPhotoHolder> firebaseRecyclerAdapter;
    private Toolbar mToolbar;
    private String mTridId;
    private Trip mTrip;
    private DatabaseReference mTripPhotosDataRef,mTripDataRef;
    private TextView mTripNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_photos);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Constants.DISPLAY_WIDTH = metrics.widthPixels;
        Constants.DISPLAY_HEIGHT = metrics.heightPixels;
        mToolbar= (Toolbar) findViewById(R.id.toolbar);
        mTripNameTextView = (TextView) findViewById(R.id.trip_name_text_view);
        setSupportActionBar(mToolbar);
        mTridId = getIntent().getStringExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY);
        mTripDataRef = FirebaseDatabase.getInstance().getReference().child("trips").child(mTridId);
        mTripDataRef.keepSynced(true);
        mTripDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTrip = dataSnapshot.getValue(Trip.class);
                mTripNameTextView.setText(mTrip.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.trip_photos_rview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),4, LinearLayoutManager.VERTICAL,false));
        mTripPhotosDataRef = FirebaseDatabase.getInstance().getReference().child("trips").child(mTridId).child("pictures");
        mTripPhotosDataRef.keepSynced(true);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Photo, TripPhotoHolder>(
                Photo.class,
                R.layout.all_photos_item,
                TripPhotoHolder.class,
                mTripPhotosDataRef
        ) {
            @Override
            protected void populateViewHolder(final TripPhotoHolder viewHolder, Photo model, final int position) {
                viewHolder.photo = model;
//                Picasso
//                        .with(getApplicationContext())
//                        .load(viewHolder.photo.getPicture_url())
//                        .networkPolicy(NetworkPolicy.OFFLINE)
//                        .centerCrop()
//                        .fit()
//                        .into(viewHolder.mTripPhotoView, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onError() {
//                        Picasso
//                                .with(getApplicationContext())
//                                .load(viewHolder.photo.getPicture_url())
//                                .centerCrop()
//                                .fit()
//                                .placeholder(R.drawable.ic_default_image)
//                                .into(viewHolder.mTripPhotoView);
//                    }
//                });
                viewHolder.target = GlideApp
                        .with(viewHolder.itemView.getContext().getApplicationContext())
                        .asBitmap()
                        .load(viewHolder.photo.getPicture_url())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_default_image)
                        .centerCrop()
                        .into(viewHolder.mTripPhotoView);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(TripPhotos.this,ImageViewActivity.class);
                        intent.setAction(Constants.ACTION.ONLINE_IMAGE_ACTION);
                        intent.putExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY,mTridId);
                        intent.putExtra(Constants.SHARE_DATA_KEYS.TRIP_IMAGE_POSITION,position);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onViewRecycled(TripPhotoHolder holder) {
                super.onViewRecycled(holder);
                holder.cleanUp();
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private static class TripPhotoHolder extends RecyclerView.ViewHolder{

        private ImageView mTripPhotoView;
        private Photo photo;
        private Target<Bitmap> target;
        public TripPhotoHolder(View itemView) {
            super(itemView);
            mTripPhotoView = (ImageView) itemView.findViewById(R.id.bitmap_image_view1);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((Constants.DISPLAY_WIDTH-8)/4,(Constants.DISPLAY_WIDTH-8)/4);
            params.setMargins(1,1,1,1);
            mTripPhotoView.setLayoutParams(params);
        }

        public void cleanUp(){
//            Picasso.with(itemView.getContext())
//                    .cancelRequest(mTripPhotoView);
//            mTripPhotoView.setImageDrawable(null);
            GlideApp.with(itemView.getContext().getApplicationContext()).clear(target);
        }
    }

    @Override
    protected void onDestroy() {
        mRecyclerView.setAdapter(null);
        firebaseRecyclerAdapter.cleanup();
        firebaseRecyclerAdapter=null;
        super.onDestroy();
    }
}
