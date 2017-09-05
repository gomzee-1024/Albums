package com.utils.gdkcorp.albums.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.fragments.Albums;
import com.utils.gdkcorp.albums.fragments.TripList;
import com.utils.gdkcorp.albums.fragments.Photos;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements Albums.OnFragmentInteractionListener,View.OnClickListener {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTablayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private MainPagerAdapter mPagerAdapter;
    private Albums mAlbums;
    private Photos mPhotos;
    private TripList mTrips;
    private FloatingActionButton mFab;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthSateListener;
    private ImageView searchIcon,friendRequest;
    private FrameLayout mRequestCountBubble;
    private TextView mRequestNumber;
    private DatabaseReference mDataFriendRequests;
    private DatabaseReference mDataCurrentUsers;
    private CircleImageView mProfilePicView;
    private DatabaseReference mConnectedRef;
    private CoordinatorLayout mRootLayout;
    private boolean mConnected=false;
    private ValueEventListener mConnectedListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mConnectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        mAuthSateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Intent intent = new Intent(MainActivity.this,LogInSignUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    initUI();
                    checkConnection();
                }
            }
        };
    }

    private void checkConnection() {
        mConnectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if(!connected){
                    mConnected = false;
                    showDisconnectedSnackBar();
                }else {
                    if(!mConnected){
                        mConnected = true;
                        showConnectedSnackBar();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mConnectedRef.addValueEventListener(mConnectedListener);
    }

    private void showConnectedSnackBar() {
        Snackbar snackBar = Snackbar.make(mRootLayout,"Now you are Connected",Snackbar.LENGTH_SHORT);
        View snackView = snackBar.getView();
        snackView.setBackgroundColor(Color.GREEN);
        TextView textView = (TextView) snackView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackBar.show();
    }

    private void showDisconnectedSnackBar() {
        Snackbar snackBar = Snackbar.make(mRootLayout,"No Internet Connection!",Snackbar.LENGTH_INDEFINITE);
        View snackView = snackBar.getView();
        snackView.setBackgroundColor(Color.RED);
        TextView textView = (TextView) snackView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthSateListener);
    }

//    private void checkReadExternalStoragePermission() {
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==
//                    PackageManager.PERMISSION_GRANTED){
//                initUI();
//            }else{
//                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
//                    Toast.makeText(this,"App. needs this permission to show photos",Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
//                        READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
//            }
//        }else{
//            initUI();
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.M)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT:
//                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
//                    initUI();
//                }else{
//                    Toast.makeText(this,"App. needs this permission to work",Toast.LENGTH_LONG).show();
//                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
//                            READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
//                }
//                break;
//            default:
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//                break;
//        }
//    }

    private void initUI() {
        mDataCurrentUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        mDataCurrentUsers.keepSynced(true);
        mDataFriendRequests = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(mAuth.getCurrentUser().getUid());
        mDataFriendRequests.keepSynced(true);
        mRootLayout = (CoordinatorLayout) findViewById(R.id.root_coordinator);
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTablayout = (TabLayout) findViewById(R.id.tab_layout);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mProfilePicView = (CircleImageView) findViewById(R.id.profile_pic_view_main);
        searchIcon = (ImageView) findViewById(R.id.search_icon);
        searchIcon.setOnClickListener(this);
        friendRequest = (ImageView) findViewById(R.id.friend_requests);
        friendRequest.setOnClickListener(this);
        mRequestCountBubble = (FrameLayout) findViewById(R.id.request_count_bubble);
        mRequestNumber = (TextView) findViewById(R.id.friend_request_number);
        setSupportActionBar(mToolbar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);
        mDataFriendRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mRequestCountBubble.setVisibility(View.VISIBLE);
                    if(dataSnapshot.getChildrenCount()<=9) {
                        mRequestNumber.setText(""+dataSnapshot.getChildrenCount());
                    }else{
                        mRequestNumber.setText("9+");
                    }
                }else{
                    if(mRequestCountBubble.getVisibility()==View.VISIBLE){
                        mRequestCountBubble.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mAlbums = Albums.newInstance();
        mPhotos = Photos.newInstance();
        mTrips = TripList.newInstance();
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTablayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTablayout));
        for(int i=0;i<mTablayout.getTabCount();++i){
            switch (i){
                case 0 : mTablayout.getTabAt(i).setIcon(R.drawable.ic_photo_library_black_24dp);
                    break;
                case 1 : mTablayout.getTabAt(i).setIcon(R.drawable.ic_view_quilt_black_24dp);
                    int tabIconColor = ContextCompat.getColor(MainActivity.this,R.color.tabUnselctedIconColor);
                    mTablayout.getTabAt(i).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 2 : mTablayout.getTabAt(i).setIcon(R.drawable.ic_photo_album_black_24dp);
                    tabIconColor = ContextCompat.getColor(MainActivity.this,R.color.tabUnselctedIconColor);
                    mTablayout.getTabAt(i).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    break;
            }
        }
        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(MainActivity.this,R.color.tabSelectedIconColor);
                if(tab.getIcon()!=null)
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(MainActivity.this,R.color.tabUnselctedIconColor);
                if(tab.getIcon()!=null)
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mDataCurrentUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                Picasso.with(MainActivity.this).load(user.getProfile_pic_url()).networkPolicy(NetworkPolicy.OFFLINE).into(mProfilePicView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(MainActivity.this).load(user.getProfile_pic_url()).into(mProfilePicView);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab :
                startActivity(new Intent(MainActivity.this,MakeTripActivity.class));
                break;
            case R.id.search_icon : startActivity(new Intent(MainActivity.this,SearchFriendActivity.class));
                break;
            case R.id.friend_requests : startActivity(new Intent(MainActivity.this,FriendRequestActivity.class));
                break;
        }
    }

    class MainPagerAdapter extends FragmentPagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//            FourthActivity.MyFragment myFragment = FourthActivity.MyFragment.newInstance(position);
            Fragment curFragment=null;
            switch (position){
                case 0:curFragment = mAlbums;
                    break;
                case 1: curFragment = mPhotos;
                    break;
                case 2: curFragment = mTrips;
                    break;
            }
            return curFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAuthSateListener!=null)
        mAuth.removeAuthStateListener(mAuthSateListener);
        if(mConnectedListener!=null)
        mConnectedRef.removeEventListener(mConnectedListener);
    }
}
