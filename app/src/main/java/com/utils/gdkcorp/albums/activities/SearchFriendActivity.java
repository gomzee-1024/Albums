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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFriendActivity extends AppCompatActivity {
    private RecyclerView mFriendRview;
    private DatabaseReference mUsersDataRef;
    private DatabaseReference mFriendsDataRef;
    private DatabaseReference mFriendRequestDataRef;
    private EditText mEditTextSearch;
    private String mSearchInput;
    private ImageView mClearEditTextView;
    private FirebaseRecyclerAdapter<User,FriendHolder> firebaseRecyclerAdapter;
    private FirebaseAuth mAuth;
    private View.OnClickListener mGenClickListener;
    private TextWatcher mTextWatcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        mAuth =FirebaseAuth.getInstance();
        mClearEditTextView = (ImageView) findViewById(R.id.clear_search);
        mClearEditTextView.setVisibility(View.INVISIBLE);
        setUpmGenClickListener();
        mClearEditTextView.setOnClickListener(mGenClickListener);
        mFriendRview = (RecyclerView) findViewById(R.id.friend_search_rview);
        mFriendRview.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        mUsersDataRef = FirebaseDatabase.getInstance().getReference().child("users");
        mFriendsDataRef = FirebaseDatabase.getInstance().getReference().child("friends");
        mFriendRequestDataRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mEditTextSearch = (EditText) findViewById(R.id.search_edit_text);
        setUpmTextWatcher();
        mEditTextSearch.addTextChangedListener(mTextWatcher);
    }

    private void setUpmTextWatcher() {
        mTextWatcher = new TextWatcher() {
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
                            mUsersDataRef.orderByChild("name_lowercase").startAt(mSearchInput).endAt(mSearchInput+"~")
                    ) {
                        @Override
                        protected void populateViewHolder(final FriendHolder viewHolder, final User model, int position) {
                            viewHolder.name.setText(model.getName());
                            viewHolder.user = model;
                            Picasso
                                    .with(viewHolder.itemView.getContext())
                                    .load(model.getProfile_pic_url())
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                    .into(viewHolder.profileImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso
                                                    .with(viewHolder.itemView.getContext())
                                                    .load(model.getProfile_pic_url())
                                                    .placeholder(R.drawable.ic_account_circle_grey_300_24dp)
                                                    .into(viewHolder.profileImageView);
                                        }
                                    });
                            DatabaseReference ref = mFriendsDataRef
                                    .child(mAuth.getCurrentUser().getUid()).child(model.getUser_id());
                            final DatabaseReference ref1 = mFriendRequestDataRef
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

                        @Override
                        public void onViewRecycled(FriendHolder holder) {
                            super.onViewRecycled(holder);
                            holder.cleanUp();
                        }
                    };

                    mFriendRview.setAdapter(firebaseRecyclerAdapter);
                }
            }
        };
    }

    private void setUpmGenClickListener() {
        mGenClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.clear_search : mEditTextSearch.setText("");
                        mClearEditTextView.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        };
    }

    public static class FriendHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView name;
        Button addFriendButton;
        User user;
        DatabaseReference mFriendRequestDataRef;
        FirebaseAuth mAuth;
        View.OnClickListener mClickListenerChild;
        public FriendHolder(View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            mFriendRequestDataRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
            profileImageView = (CircleImageView) itemView.findViewById(R.id.friend_profile_image_view);
            name = (TextView) itemView.findViewById(R.id.friend_name);
            addFriendButton = (Button) itemView.findViewById(R.id.send_request_button);
            setUpmClickListenerChild();
            addFriendButton.setOnClickListener(mClickListenerChild);
        }

        private void setUpmClickListenerChild() {
            mClickListenerChild = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference ref = mFriendRequestDataRef.child(user.getUser_id()).child(mAuth.getCurrentUser().getUid());
                    ref.child("user_id").setValue(mAuth.getCurrentUser().getUid());
                    addFriendButton.setText("SENT");
                    addFriendButton.setTextColor(Color.BLACK);
                    addFriendButton.setBackground(view.getContext().getDrawable(R.drawable.button_background_disabled));
                    addFriendButton.setEnabled(false);
                }
            };
        }

        public void cleanUp(){
            Picasso.with(itemView.getContext())
                    .cancelRequest(profileImageView);
            profileImageView.setImageDrawable(null);
            user = null;
        }
    }

    @Override
    protected void onDestroy() {
        mGenClickListener = null;
        mFriendRview.setAdapter(null);
        firebaseRecyclerAdapter.cleanup();
        firebaseRecyclerAdapter=null;
        mTextWatcher = null;
        super.onDestroy();
    }
}
