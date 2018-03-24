package com.utils.gdkcorp.albums.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripAddFriendActivity extends AppCompatActivity {

    private ChipsInput chipsInput;
    private Button applyButton;
    private DatabaseReference mCurrentFriendsDataRef;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter<User,FriendTripHolder> firebaseRecyclerAdapter;
    private View.OnClickListener mClickListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_add_friend);
        mAuth = FirebaseAuth.getInstance();
        mCurrentFriendsDataRef = FirebaseDatabase.getInstance().getReference().child("friends").child(mAuth.getCurrentUser().getUid());
        mCurrentFriendsDataRef.keepSynced(true);
        chipsInput = (ChipsInput) findViewById(R.id.chip_input);
        applyButton = (Button) findViewById(R.id.apply);
        setUpmClickListener();
        applyButton.setOnClickListener(mClickListener);
        mRecyclerView = (RecyclerView) findViewById(R.id.friend_add_trip_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chipInterface, int i) {

            }

            @Override
            public void onChipRemoved(ChipInterface chipInterface, int i) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence) {
                if(firebaseRecyclerAdapter!=null){
                    firebaseRecyclerAdapter.cleanup();
                }
                if(charSequence.toString().equals("")|| charSequence.toString().length()<2) {
                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FriendTripHolder>(
                            User.class,
                            R.layout.add_friend_to_trip_item,
                            FriendTripHolder.class,
                            mCurrentFriendsDataRef
                    ) {
                        @Override
                        protected void populateViewHolder(FriendTripHolder viewHolder, final User model, int position) {
                            viewHolder.name.setText(model.getName());
                            viewHolder.friend = model;
                            Picasso.with(viewHolder.mItemView.getContext().getApplicationContext())
                                    .load(model.getProfile_pic_url())
                                    .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                    .into(viewHolder.imageView);
                            viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    chipsInput.addChip(model);
                                }
                            });
                        }

                        @Override
                        public void onViewRecycled(FriendTripHolder holder) {
                            super.onViewRecycled(holder);
                            holder.cleanUp();
                        }
                    };
                    mRecyclerView.setAdapter(firebaseRecyclerAdapter);
                }else{
                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FriendTripHolder>(
                            User.class,
                            R.layout.add_friend_to_trip_item,
                            FriendTripHolder.class,
                            mCurrentFriendsDataRef
                                    .orderByChild("name_lowercase")
                                    .startAt(charSequence.toString())
                                    .endAt(charSequence.toString()+"~")
                    ) {
                        @Override
                        protected void populateViewHolder(final FriendTripHolder viewHolder, final User model, final int position) {
                            viewHolder.name.setText(model.getName());
                            viewHolder.friend = model;
                            Picasso
                                    .with(viewHolder.mItemView.getContext().getApplicationContext())
                                    .load(model.getProfile_pic_url())
                                    .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                    .into(viewHolder.imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso
                                                    .with(viewHolder.mItemView.getContext().getApplicationContext())
                                                    .load(model.getProfile_pic_url())
                                                    .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                                    .into(viewHolder.imageView);
                                        }
                                    });
                            viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    chipsInput.addChip(model);
                                }
                            });
                        }

                        @Override
                        public void onViewRecycled(FriendTripHolder holder) {
                            super.onViewRecycled(holder);
                            holder.cleanUp();
                        }
                    };
                    mRecyclerView.setAdapter(firebaseRecyclerAdapter);
                }
            }
        });
    }

    private void setUpmClickListener() {
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.apply :
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        ArrayList<User> arrayList = new ArrayList<User>();
                        for(ChipInterface chip : chipsInput.getSelectedChipList()){
                            User user = (User) chip;
                            arrayList.add(user);
                        }
                        if(arrayList.size()!=0) {
                            bundle.putParcelableArrayList("data", arrayList);
                            intent.putExtras(bundle);
                            setResult(RESULT_OK, intent);
                        }
                        finish();
                }
            }
        };
    }


    public static class FriendTripHolder extends RecyclerView.ViewHolder{
        private CircleImageView imageView;
        private TextView name;
        User friend;
        View mItemView;
        public FriendTripHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            imageView = (CircleImageView) itemView.findViewById(R.id.friend_profile_trip);
            name = (TextView) itemView.findViewById(R.id.friend_name_trip);
        }

        public void cleanUp(){
            Picasso.with(itemView.getContext().getApplicationContext())
                    .cancelRequest(imageView);
            imageView.setImageDrawable(null);
        }

    }

    @Override
    protected void onDestroy() {
        mRecyclerView.setAdapter(null);
        if(firebaseRecyclerAdapter!=null) {
            firebaseRecyclerAdapter.cleanup();
        }
        firebaseRecyclerAdapter = null;
        super.onDestroy();
    }
}
