package com.utils.gdkcorp.albums.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.Utils.Utils;
import com.utils.gdkcorp.albums.activities.TripPhotos;
import com.utils.gdkcorp.albums.customviews.MyItemDecoration;
import com.utils.gdkcorp.albums.models.Photo;
import com.utils.gdkcorp.albums.models.Trip;
import com.utils.gdkcorp.albums.models.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
    private RecyclerView mRecyclerView;
    private DatabaseReference mTripsDataRef;
    private DatabaseReference mCurrentUserTripsDataRef;
    private FirebaseRecyclerAdapter<String,TripItemHolder> mAdapter;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mTripRViewState;

    public TripList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripList.
     */
    // TODO: Rename and change types and number of parameters
    public static TripList newInstance(Parcelable tripRviewState) {
        TripList fragment = new TripList();
        Bundle args = new Bundle();
        args.putParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET, tripRviewState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.i("TripList","ArgumentsNotNull");
            mTripRViewState = getArguments().getParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY, Context.MODE_PRIVATE);
        String user_id = preferences.getString(Constants.USER.USER_ID_PREFERENCE_KEY,null);
        mTripsDataRef = FirebaseDatabase.getInstance().getReference().child("trips");
        mCurrentUserTripsDataRef = FirebaseDatabase.getInstance().getReference().child("user_trips").child(user_id);
        mCurrentUserTripsDataRef.keepSynced(true);
        mProgressBar = (ProgressBar) view.findViewById(R.id.trip_list_progess_bar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.trip_list_rview);
        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new MyItemDecoration((int)Utils.dipToPixels(getContext(),8f),Constants.DIRECTION.UP));
        mAdapter = new FirebaseRecyclerAdapter<String, TripItemHolder>(
                String.class,
                R.layout.trip_item,
                TripItemHolder.class,
                mCurrentUserTripsDataRef
        ) {
            @Override
            protected void populateViewHolder(final TripItemHolder viewHolder, final String model, int position) {
                if(mProgressBar.getVisibility()==View.VISIBLE){
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                viewHolder.mTripPhotoDataRef = mTripsDataRef.child(model);
                viewHolder.mTripPhotoDataRef.keepSynced(true);
                mTripsDataRef.child(model).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Trip trip = dataSnapshot.getValue(Trip.class);
                        viewHolder.trip_id = model;
                        viewHolder.mTripName.setText(trip.getName());
                        viewHolder.mTripLocaion.setText(trip.getLocation());
                        SimpleDateFormat curFormat = new SimpleDateFormat("yyyyMMdd");
                        Date date=null;
                        try {
                            date = curFormat.parse(trip.getDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat finalFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String dateFinal = finalFormat.format(date);
                        viewHolder.mTripDate.setText(dateFinal);
                        viewHolder.setUpTripPhotosValueEventListener();
                        viewHolder.mTripPhotoDataRef.child("pictures").addValueEventListener(viewHolder.mTripPhotosValueEventListener);
                        viewHolder.mTripFriendsDataRef = mTripsDataRef.child(model).child("friends");
                        viewHolder.mTripFriendsDataRef.keepSynced(true);
                        viewHolder.mChildFirebaseAdapter = new FirebaseRecyclerAdapter<String, TripItemHolder.FriendImageHolder>(
                                String.class,
                                R.layout.circular_image_item,
                                TripItemHolder.FriendImageHolder.class,
                                viewHolder.mTripFriendsDataRef
                        ) {
                            @Override
                            protected void populateViewHolder(final TripItemHolder.FriendImageHolder viewHolder, String model, int position) {
                                viewHolder.mTripUserFriendsDataRef = FirebaseDatabase.getInstance().getReference().child("users").child(model);
                                viewHolder.mTripUserFriendsDataRef.keepSynced(true
                                );
                                viewHolder.mTripUserFriendsDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final User user = dataSnapshot.getValue(User.class);
                                        Picasso
                                                .with(getContext())
                                                .load(user.getProfile_pic_url())
                                                .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                                .into(viewHolder.mCircleImageView, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso
                                                                .with(getContext())
                                                                .load(user.getProfile_pic_url())
                                                                .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                                                .into(viewHolder.mCircleImageView);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onViewRecycled(TripItemHolder.FriendImageHolder holder) {
                                super.onViewRecycled(holder);
                                holder.cleanUp();
                            }
                        };
                        viewHolder.mFriendsRecyclerView.setAdapter(viewHolder.mChildFirebaseAdapter);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), TripPhotos.class);
                                intent.putExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY,viewHolder.trip_id);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onViewRecycled(TripItemHolder holder) {
                super.onViewRecycled(holder);
                holder.cleanUp();
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(mTripRViewState);
            }
        });
    }

    public static class TripItemHolder extends RecyclerView.ViewHolder{

        String trip_id;
        ImageView imageView1,imageView2,imageView3;
        RecyclerView mFriendsRecyclerView;
        TextView mTripName,mTripLocaion,mTripDate;
        FrameLayout mNoImageLayout;
        RelativeLayout mFormat1,mFormat2,mFormat3;
        FirebaseRecyclerAdapter<String,FriendImageHolder> mChildFirebaseAdapter;
        DatabaseReference mTripPhotoDataRef,mTripFriendsDataRef;
        ValueEventListener mTripPhotosValueEventListener;
        public TripItemHolder(View itemView) {
            super(itemView);
            mFriendsRecyclerView = (RecyclerView) itemView.findViewById(R.id.trip_item_friends_rview);
            mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(),LinearLayoutManager.HORIZONTAL,false));
            mFriendsRecyclerView.addItemDecoration(new MyItemDecoration((int)Utils.dipToPixels(itemView.getContext(),12f),Constants.DIRECTION.LEFT));
            mTripName = (TextView) itemView.findViewById(R.id.trip_item_trip_name);
            mTripLocaion = (TextView) itemView.findViewById(R.id.trip_item_location);
            mTripDate = (TextView) itemView.findViewById(R.id.trip_item_date);
            mFormat1 = (RelativeLayout) itemView.findViewById(R.id.trip_one_relative_layout);
            mFormat2 = (RelativeLayout) itemView.findViewById(R.id.trip_two_relative_layout);
            mFormat3 = (RelativeLayout) itemView.findViewById(R.id.trip_three_relative_layout);
            mNoImageLayout = (FrameLayout) itemView.findViewById(R.id.no_images);
        }

        public static class FriendImageHolder extends RecyclerView.ViewHolder{
            CircleImageView mCircleImageView;
            DatabaseReference mTripUserFriendsDataRef;
            public FriendImageHolder(View itemView) {
                super(itemView);
                mCircleImageView = (CircleImageView) itemView.findViewById(R.id.circle_image_item);
            }
            public void cleanUp(){
                Picasso.with(itemView.getContext())
                        .cancelRequest(mCircleImageView);
                mCircleImageView.setImageDrawable(null);
            }
        }

        public void setUpTripPhotosValueEventListener(){
            mTripPhotosValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount()>=3){
                        mNoImageLayout.setVisibility(View.GONE);
                        mFormat2.setVisibility(View.GONE);
                        mFormat1.setVisibility(View.GONE);
                        mFormat3.setVisibility(View.VISIBLE);
                        imageView1 = (ImageView) itemView.findViewById(R.id.trip_three_image_1);
                        imageView2 = (ImageView) itemView.findViewById(R.id.trip_three_image_2);
                        imageView3 = (ImageView) itemView.findViewById(R.id.trip_three_image_3);
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        DataSnapshot dataSnapshot1 = iterator.next();
                        Photo picture = dataSnapshot1.getValue(Photo.class);
                        final Photo finalPicture = picture;
                        Picasso
                                .with(itemView.getContext())
                                .load(picture.getPicture_url())
                                .placeholder(R.drawable.ic_default_image)
                                .into(imageView1, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso
                                                .with(itemView.getContext())
                                                .load(finalPicture.getPicture_url())
                                                .placeholder(R.drawable.ic_default_image)
                                                .into(imageView1);
                                    }
                                });
                        dataSnapshot1 = iterator.next();
                        picture = dataSnapshot1.getValue(Photo.class);
                        final Photo finalPicture1 = picture;
                        Picasso
                                .with(itemView.getContext())
                                .load(picture.getPicture_url())
                                .placeholder(R.drawable.ic_default_image)
                                .into(imageView2, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso
                                                .with(itemView.getContext())
                                                .load(finalPicture1.getPicture_url())
                                                .placeholder(R.drawable.ic_default_image)
                                                .into(imageView2);
                                    }
                                });
                        dataSnapshot1 = iterator.next();
                        picture = dataSnapshot1.getValue(Photo.class);
                        final Photo finalPicture2 = picture;
                        Picasso
                                .with(itemView.getContext())
                                .load(picture.getPicture_url())
                                .placeholder(R.drawable.ic_default_image)
                                .into(imageView3, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso
                                                .with(itemView.getContext())
                                                .load(finalPicture2.getPicture_url())
                                                .placeholder(R.drawable.ic_default_image)
                                                .into(imageView3);
                                    }
                                });
                    }else if(dataSnapshot.getChildrenCount()>=2){
                        mNoImageLayout.setVisibility(View.GONE);
                        mFormat1.setVisibility(View.GONE);
                        mFormat3.setVisibility(View.GONE);
                        mFormat2.setVisibility(View.VISIBLE);
                        imageView1 = (ImageView) itemView.findViewById(R.id.trip_two_image_1);
                        imageView2 = (ImageView) itemView.findViewById(R.id.trip_two_image_2);
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        DataSnapshot dataSnapshot1 = iterator.next();
                        Photo picture = dataSnapshot1.getValue(Photo.class);
                        final Photo finalPicture = picture;
                        Picasso
                                .with(itemView.getContext())
                                .load(picture.getPicture_url())
                                .placeholder(R.drawable.ic_default_image)
                                .into(imageView1, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso
                                                .with(itemView.getContext())
                                                .load(finalPicture.getPicture_url())
                                                .placeholder(R.drawable.ic_default_image)
                                                .into(imageView1);
                                    }
                                });
                        dataSnapshot1 = iterator.next();
                        picture = dataSnapshot1.getValue(Photo.class);
                        final Photo finalPicture1 = picture;
                        Picasso
                                .with(itemView.getContext())
                                .load(picture.getPicture_url())
                                .placeholder(R.drawable.ic_default_image)
                                .into(imageView2, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso
                                                .with(itemView.getContext())
                                                .load(finalPicture1.getPicture_url())
                                                .placeholder(R.drawable.ic_default_image)
                                                .into(imageView2);
                                    }
                                });
                    }else if(dataSnapshot.getChildrenCount()>=1){
                        mNoImageLayout.setVisibility(View.GONE);
                        mFormat3.setVisibility(View.GONE);
                        mFormat2.setVisibility(View.GONE);
                        mFormat1.setVisibility(View.VISIBLE);
                        imageView1 = (ImageView) itemView.findViewById(R.id.trip_one_image_1);
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        DataSnapshot dataSnapshot1 = iterator.next();
                        final Photo picture = dataSnapshot1.getValue(Photo.class);
                        Picasso
                                .with(itemView.getContext())
                                .load(picture.getPicture_url())
                                .placeholder(R.drawable.ic_default_image)
                                .into(imageView1, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso
                                                .with(itemView.getContext())
                                                .load(picture.getPicture_url())
                                                .placeholder(R.drawable.ic_default_image)
                                                .into(imageView1);
                                    }
                                });
                    }else{
                        mFormat1.setVisibility(View.GONE);
                        mFormat3.setVisibility(View.GONE);
                        mFormat2.setVisibility(View.GONE);
                        mNoImageLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }

        public void cleanUp(){
            if(imageView1!=null)
            imageView1.setImageDrawable(null);
            if(imageView2!=null)
            imageView2.setImageDrawable(null);
            if(imageView3!=null)
            imageView3.setImageDrawable(null);
            if(mChildFirebaseAdapter!=null)
            mChildFirebaseAdapter.cleanup();
            if(mTripFriendsDataRef!=null){
                mTripPhotoDataRef.child("pictures").removeEventListener(mTripPhotosValueEventListener);
            }
        }
    }

    public Parcelable getState(){
        return mRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.setAdapter(null);
        if(mAdapter!=null){
            mAdapter.cleanup();
        }
        mAdapter=null;
        super.onDestroyView();
    }
}
