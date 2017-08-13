package com.utils.gdkcorp.albums.activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.utils.gdkcorp.albums.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFriendActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mFriendRview;
    private DatabaseReference mDataRef;
    private EditText mEditTextSearch;
    private String mSearchInput;
    private ImageView mClearEditTextView;
    private FirebaseRecyclerAdapter<User,FriendHolder> firebaseRecyclerAdapter;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        mClearEditTextView = (ImageView) findViewById(R.id.clear_search);
        mClearEditTextView.setVisibility(View.INVISIBLE);
        mClearEditTextView.setOnClickListener(this);
        mFriendRview = (RecyclerView) findViewById(R.id.friend_search_rview);
        mFriendRview.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mDataRef = FirebaseDatabase.getInstance().getReference();
        mAuth =FirebaseAuth.getInstance();
        mEditTextSearch = (EditText) findViewById(R.id.search_edit_text);
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchInput = mEditTextSearch.getText().toString().toLowerCase();
                if(firebaseRecyclerAdapter!=null){
                    firebaseRecyclerAdapter.cleanup();
                }
                if(mSearchInput.equals("")|| mSearchInput.length()<2) {
                    mFriendRview.setAdapter(null);
                }else{
                    mClearEditTextView.setVisibility(View.VISIBLE);
                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FriendHolder>(
                            User.class,
                            R.layout.friend_search_item,
                            FriendHolder.class,
                            mDataRef.child("users").orderByChild("name_lowercase").startAt(mSearchInput).endAt(mSearchInput+"~")
                    ) {
                        @Override
                        protected void populateViewHolder(final FriendHolder viewHolder, User model, int position) {
                            viewHolder.name.setText(model.getName());
                            viewHolder.user = model;
                            Picasso.with(viewHolder.itemView.getContext()).load(model.getProfile_pic_url()).placeholder(R.drawable.ic_account_circle_grey_300_24dp).into(viewHolder.profileImageView);
                            DatabaseReference ref = mDataRef.child("friends")
                                    .child(mAuth.getCurrentUser().getUid()).child(model.getUser_id());
                            final DatabaseReference ref1 = mDataRef.child("friend_requests")
                                    .child(model.getUser_id()).child(mAuth.getCurrentUser().getUid());

                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        viewHolder.addFriendButton.setText("FRIENDS");
                                        viewHolder.addFriendButton.setTextColor(Color.BLACK);
                                        viewHolder.addFriendButton.setBackground(getDrawable(R.drawable.button_background_disabled));
                                        viewHolder.addFriendButton.setEnabled(false);
                                    }else{
                                        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    viewHolder.addFriendButton.setText("SENT");
                                                    viewHolder.addFriendButton.setTextColor(Color.BLACK);
                                                    viewHolder.addFriendButton.setBackground(getDrawable(R.drawable.button_background_disabled));
                                                    viewHolder.addFriendButton.setEnabled(false);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    };

                    mFriendRview.setAdapter(firebaseRecyclerAdapter);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.clear_search : mEditTextSearch.setText("");
                mClearEditTextView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public static class FriendHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView profileImageView;
        TextView name;
        Button addFriendButton;
        User user;
        DatabaseReference mRef;
        FirebaseAuth mAuth;
        public FriendHolder(View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            mRef = FirebaseDatabase.getInstance().getReference();
            profileImageView = (CircleImageView) itemView.findViewById(R.id.friend_profile_image_view);
            name = (TextView) itemView.findViewById(R.id.friend_name);
            addFriendButton = (Button) itemView.findViewById(R.id.send_request_button);
            addFriendButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            DatabaseReference ref = mRef.child("friend_requests").child(user.getUser_id()).child(mAuth.getCurrentUser().getUid());
            ref.child("user_id").setValue(mAuth.getCurrentUser().getUid());
            addFriendButton.setText("SENT");
            addFriendButton.setTextColor(Color.BLACK);
            addFriendButton.setBackground(view.getContext().getDrawable(R.drawable.button_background_disabled));
            addFriendButton.setEnabled(false);
        }
    }
}
