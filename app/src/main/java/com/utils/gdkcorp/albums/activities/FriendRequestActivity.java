package com.utils.gdkcorp.albums.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.FriendRequest;
import com.utils.gdkcorp.albums.models.User;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity {
    private RecyclerView mFriendRequestRview;
    private DatabaseReference mDataRef;
    private ImageView mClearEditTextView;
    private FirebaseRecyclerAdapter<FriendRequest,FriendRequestHolder> firebaseRecyclerAdapter;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDataRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mFriendRequestRview = (RecyclerView) findViewById(R.id.friend_requests_rview);
        mFriendRequestRview.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendRequest, FriendRequestHolder>(
                FriendRequest.class,
                R.layout.friend_request_item,
                FriendRequestHolder.class,
                mDataRef.child("friend_requests").child(mAuth.getCurrentUser().getUid())
        ) {
            @Override
            protected void populateViewHolder(final FriendRequestHolder viewHolder, final FriendRequest model, int position) {
                DatabaseReference ref = mDataRef.child("users").child(model.getUser_id());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        viewHolder.name.setText(user.getName());
                        viewHolder.friend = user;
                        Picasso.with(viewHolder.itemView.getContext()).load(user.getProfile_pic_url()).placeholder(R.drawable.ic_account_circle_grey_300_24dp).into(viewHolder.profileImageView);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mFriendRequestRview.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendRequestHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView profileImageView;
        TextView name;
        Button acceptFriendButton;
        User friend;
        DatabaseReference mRef;
        FirebaseAuth mAuth;
        public FriendRequestHolder(View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            mRef = FirebaseDatabase.getInstance().getReference();
            profileImageView = (CircleImageView) itemView.findViewById(R.id.friend_profile_image_view_request);
            name = (TextView) itemView.findViewById(R.id.friend_name_request);
            acceptFriendButton = (Button) itemView.findViewById(R.id.accept_request_button);
            acceptFriendButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final DatabaseReference ref = mRef.child("friends").child(mAuth.getCurrentUser().getUid()).child(friend.getUser_id());
            ref.child("user_id").setValue(friend.getUser_id());
            ref.child("name_lowercase").setValue(friend.getName_lowercase());
            ref.child("name").setValue(friend.getName());
            ref.child("profile_pic_url").setValue(friend.getProfile_pic_url());
            ref.child("registration_token").setValue(friend.getRegistration_token());
            final DatabaseReference ref2 = mRef.child("friends").child(friend.getUser_id()).child(mAuth.getCurrentUser().getUid());
            mRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    ref2.child("user_id").setValue(user.getUser_id());
                    ref2.child("name_lowercase").setValue(user.getName_lowercase());
                    ref2.child("name").setValue(user.getName());
                    ref2.child("profile_pic_url").setValue(user.getProfile_pic_url());
                    ref2.child("registration_token").setValue(user.getRegistration_token());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference ref1 = mRef.child("friend_requests").child(mAuth.getCurrentUser().getUid());
            ref1.child(friend.getUser_id()).removeValue();
            acceptFriendButton.setText("Accepted");
            acceptFriendButton.setTextColor(Color.BLACK);
            acceptFriendButton.setBackground(view.getContext().getDrawable(R.drawable.button_background_disabled));
        }
    }

}
