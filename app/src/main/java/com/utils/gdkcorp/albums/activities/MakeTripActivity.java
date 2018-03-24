package com.utils.gdkcorp.albums.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.adapters.ChipAdapter;
import com.utils.gdkcorp.albums.models.User;
import com.utils.gdkcorp.albums.services.TripService;

import java.util.ArrayList;

public class MakeTripActivity extends AppCompatActivity {

    private EditText mTripName,mTripLocation;
    private ImageView addFriend;
    private RecyclerView mRecyclerView;
    private ArrayList<User> mList= new ArrayList<User>();
    private ChipAdapter mChipAdapter;
    private FrameLayout mNoFriendLayout;
    private Button mCreateTripButton;
    private View.OnClickListener mClickListener;
    private ChipAdapter.ChipAdapterInterface mChipClickListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_trip);
        mRecyclerView = (RecyclerView) findViewById(R.id.make_trip_rview);
        addFriend = (ImageView) findViewById(R.id.add_friend_to_trip);
        mNoFriendLayout = (FrameLayout) findViewById(R.id.no_friends_layout);
        mCreateTripButton = (Button) findViewById(R.id.create_trip_button);
        mTripName = (EditText) findViewById(R.id.make_trip_name);
        mTripLocation = (EditText) findViewById(R.id.make_trip_location);
        setUpmClickListener();
        mCreateTripButton.setOnClickListener(mClickListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        setUpmChipClickListener();
        mChipAdapter = new ChipAdapter(mList,mChipClickListener);
        mRecyclerView.setAdapter(mChipAdapter);
        addFriend.setOnClickListener(mClickListener);
    }

    private void setUpmChipClickListener() {
        mChipClickListener = new ChipAdapter.ChipAdapterInterface() {
            @Override
            public void onClickRemove(View v, int position) {
                if(mChipAdapter.getItemCount()==1){
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mNoFriendLayout.setVisibility(View.VISIBLE);
                    mChipAdapter.remove(position);
                }
            }
        };
    }

    private void setUpmClickListener() {
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.add_friend_to_trip :
                        Intent intent = new Intent(MakeTripActivity.this,TripAddFriendActivity.class);
                        startActivityForResult(intent,2);
                        break;
                    case R.id.create_trip_button :
                        String name = mTripName.getText().toString();
                        String location = mTripLocation.getText().toString();
                        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(location)) {
                            Intent intent1 = new Intent(MakeTripActivity.this, TripService.class);
                            intent1.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.SHARE_DATA_KEYS.TRIP_NAME_KEY,name);
                            bundle.putString(Constants.SHARE_DATA_KEYS.TRIP_LOCATION_KEY,location);
                            bundle.putParcelableArrayList(Constants.SHARE_DATA_KEYS.SHARE_DATA_KEY, mChipAdapter.getSelectedUserList());
                            intent1.putExtras(bundle);
                            startService(intent1);
                            finish();
                        }
                }
            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==2){
            if(resultCode==RESULT_OK) {
                Bundle bundle = data.getExtras();
                ArrayList<User> list;
                list = bundle.getParcelableArrayList("data");
                Log.i("MakeTripActivity", "" + mList.size());
                mChipAdapter.addAll(list);
                mNoFriendLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        mRecyclerView.setAdapter(null);
        mClickListener =null;
        mChipClickListener=null;
        super.onDestroy();
    }
}
