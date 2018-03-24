package com.utils.gdkcorp.albums.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.GlideApp;
import com.utils.gdkcorp.albums.fragments.Albums;
import com.utils.gdkcorp.albums.fragments.TripList;
import com.utils.gdkcorp.albums.fragments.Photos;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

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
    private ImageView searchIcon,friendRequest;
    private FrameLayout mRequestCountBubble;
    private TextView mRequestNumber;
    private DatabaseReference mDataFriendRequests;
    private DatabaseReference mDataCurrentUsers;
    private CircleImageView mProfilePicView;
    private DatabaseReference mConnectedRef;
    private CoordinatorLayout mRootLayout;
    private boolean mConnected=false;
    private ValueEventListener mConnectedListener,mNoFriendRequestListener,mProfilePicListener;
    private Uri mProfilePicUri;
    private StorageReference mStoreRef;
    private MyAuthStateListener mAuthStateListener;
    private View.OnClickListener mGenClickListener;
    private android.support.v7.widget.PopupMenu.OnMenuItemClickListener mMenuItemClickListener;
    public Intent logInIntent;
    private Parcelable mPhotosState,mTripListState,mAlbumsMainState;
    private Parcelable[] mAlbumsChildStates;
    private int curFragmentPosition;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i("MainActivity","onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Log.i("MainActivity","onResume");
        super.onResume();
        if(mViewPager!=null) {
            Log.i("MainActivity","curPage "+curFragmentPosition);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(curFragmentPosition);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity","onCreate");
        setContentView(R.layout.activity_main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Constants.DISPLAY_WIDTH = metrics.widthPixels;
        Constants.DISPLAY_HEIGHT = metrics.heightPixels;
        mConnectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        mAuth = FirebaseAuth.getInstance();
        logInIntent = new Intent(getApplicationContext(),LogInSignUpActivity.class);
    }

    private void setUpmAuthStateListener() {
        mAuthStateListener = new MyAuthStateListener(this);
    }

    public void finishActivity(){
        finish();
    }

    private static class MyAuthStateListener implements FirebaseAuth.AuthStateListener{

        private MainActivity mainActivity;

        public MyAuthStateListener(MainActivity context){
            this.mainActivity = context;
        }

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if(firebaseAuth.getCurrentUser()==null){
                mainActivity.startActivity(mainActivity.logInIntent);
                mainActivity.finishActivity();
            }else{
                mainActivity.initUI();
                mainActivity.initDatabases();
                mainActivity.checkConnection();
            }
        }

        public void cleanUp(){
            mainActivity=null;
        }
    }

    public void initDatabases() {
        mDataCurrentUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        mDataCurrentUsers.keepSynced(true);
        mDataFriendRequests = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(mAuth.getCurrentUser().getUid());
        mDataFriendRequests.keepSynced(true);
        setUpNoFriendRequestValueEnventListener();
        mDataFriendRequests.addValueEventListener(mNoFriendRequestListener);
        setUpmProfilePicListener();
        mDataCurrentUsers.addValueEventListener(mProfilePicListener);
    }

    public void checkConnection() {
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
        setUpmAuthStateListener();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    public void initUI() {
        mRootLayout = (CoordinatorLayout) findViewById(R.id.root_coordinator);
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTablayout = (TabLayout) findViewById(R.id.tab_layout);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mProfilePicView = (CircleImageView) findViewById(R.id.profile_pic_view_main);
        setUpmGenClickListener();
        mProfilePicView.setOnClickListener(mGenClickListener);
        searchIcon = (ImageView) findViewById(R.id.search_icon);
        searchIcon.setOnClickListener(mGenClickListener);
        friendRequest = (ImageView) findViewById(R.id.friend_requests);
        friendRequest.setOnClickListener(mGenClickListener);
        mRequestCountBubble = (FrameLayout) findViewById(R.id.request_count_bubble);
        mRequestNumber = (TextView) findViewById(R.id.friend_request_number);
        setSupportActionBar(mToolbar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(mGenClickListener);
        mAlbums = Albums.newInstance(mAlbumsMainState,mAlbumsChildStates);
        mPhotos = Photos.newInstance(mPhotosState);
        mTrips = TripList.newInstance(mTripListState);
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTablayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTablayout));
        for(int i=0;i<mTablayout.getTabCount();++i){
            switch (i){
                case 0 : mTablayout.getTabAt(i).setIcon(R.drawable.ic_photo_library_black_24dp);
                    int tabIconColor = ContextCompat.getColor(getApplicationContext(),R.color.tabSelectedIconColor);
                    mTablayout.getTabAt(i).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 1 : mTablayout.getTabAt(i).setIcon(R.drawable.ic_view_quilt_black_24dp);
                    tabIconColor = ContextCompat.getColor(getApplicationContext(),R.color.tabUnselctedIconColor);
                    mTablayout.getTabAt(i).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 2 : mTablayout.getTabAt(i).setIcon(R.drawable.ic_photo_album_black_24dp);
                    tabIconColor = ContextCompat.getColor(getApplicationContext(),R.color.tabUnselctedIconColor);
                    mTablayout.getTabAt(i).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    break;
            }
        }
        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(getApplicationContext(),R.color.tabSelectedIconColor);
                if(tab.getIcon()!=null)
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(getApplicationContext(),R.color.tabUnselctedIconColor);
                if(tab.getIcon()!=null)
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setUpmProfilePicListener() {
        mProfilePicListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
//                Picasso.with(getApplicationContext())
//                        .load(user.getProfile_pic_url())
//                        .networkPolicy(NetworkPolicy.OFFLINE)
//                        .into(mProfilePicView, new Callback() {
//                            @Override
//                            public void onSuccess() {
//
//                            }
//
//                            @Override
//                            public void onError() {
//                                Picasso.with(getApplicationContext())
//                                        .load(user.getProfile_pic_url())
//                                        .into(mProfilePicView);
//                            }
//                        });
                GlideApp
                        .with(getApplicationContext())
                        .load(user.getProfile_pic_url())
                        .placeholder(R.drawable.avatar)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(mProfilePicView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void setUpNoFriendRequestValueEnventListener() {
        mNoFriendRequestListener = new ValueEventListener() {
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
        };
    }

    private void setUpmGenClickListener() {
        mGenClickListener = new View.OnClickListener() {
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
                    case R.id.profile_pic_view_main : showPopupMenu(view);
                        break;
                }
            }
        };
    }


    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this,view);
        setUpmMenuItemClickListener();
        popup.setOnMenuItemClickListener(mMenuItemClickListener);
        MenuInflater menuInflator = popup.getMenuInflater();
        menuInflator.inflate(R.menu.profile_popup_menu,popup.getMenu());
        popup.show();
    }

    private void setUpmMenuItemClickListener() {
        mMenuItemClickListener = new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.change_profile_pic : changeProfilePic();
                        break;
                    case R.id.sign_out : signOut();
                        break;
                }
                return true;
            }
        };
    }


    private void signOut() {
        mAuth.signOut();
    }

    private void changeProfilePic() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setRequestedSize(720,720)
                .setAspectRatio(500,500)
                .setFixAspectRatio(true)
                .start(this);
    }

    class MainPagerAdapter extends FragmentStatePagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//            FourthActivity.MyFragment myFragment = FourthActivity.MyFragment.newInstance(position);
            Fragment curFragment=null;
            Bundle bundle = new Bundle();
            switch (position){
                case 0:
                    bundle.putParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET,mAlbumsMainState);
                    bundle.putParcelableArray(Constants.SHARE_DATA_KEYS.CHILD_RVIEW_OFFSET,mAlbumsChildStates);
                    mAlbums.setArguments(bundle);
                    curFragment = mAlbums;
                    break;
                case 1:
                    bundle.putParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET,mPhotosState);
                    mPhotos.setArguments(bundle);
                    curFragment = mPhotos;
                    break;
                case 2:
                    bundle.putParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET,mTripListState);
                    mTrips.setArguments(bundle);
                    curFragment = mTrips;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfilePicUri = result.getUri();
                Log.i("MainActivity","mProfilePicURI = "+mProfilePicUri.toString());
                //mProfilePicView.setImageURI(mProfilePicUri);
                storeProfilePicToDB();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i("MainActivity","Crop Error");
            }
        }
    }

    private void storeProfilePicToDB() {
        mStoreRef = FirebaseStorage.getInstance().getReference().child("photos");
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY,MODE_PRIVATE);
        String user_id = preferences.getString(Constants.USER.USER_ID_PREFERENCE_KEY,"user_id");
        Log.i("MainActivity",user_id);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        if(mProfilePicUri!=null && !user_id.equalsIgnoreCase("user_id")) {
            mStoreRef.child(mProfilePicUri.getLastPathSegment()).putFile(mProfilePicUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.child("profile_pic_url").setValue(taskSnapshot.getDownloadUrl().toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("MainActivity","Profile Pic Upload Failed");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("MainActivity","onDestroy");
        if(mNoFriendRequestListener!=null)
            mDataFriendRequests.removeEventListener(mNoFriendRequestListener);
        if(mProfilePicListener!=null)
            mDataCurrentUsers.removeEventListener(mProfilePicListener);
        if(mViewPager!=null){
            mViewPager.setAdapter(null);
        }
        mGenClickListener = null;
        mMenuItemClickListener = null;
        if(mConnectedListener!=null)
            mConnectedRef.removeEventListener(mConnectedListener);
        if(mAuthStateListener!=null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
            mAuthStateListener.cleanUp();
            mAuthStateListener=null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Log.i("MainActivity","onPause");
        if(mViewPager!=null)
        curFragmentPosition = mViewPager.getCurrentItem();
        if(mAlbums!=null&&mAlbums.isAdded()) {
            Log.i("MainActivity","saveAlbumsState");
            mAlbumsMainState = mAlbums.getMainRecyclerScrollOffset();
            mAlbumsChildStates = mAlbums.getChildScrollState();
        }
        if(mPhotos!=null&&mPhotos.isAdded()){
            Log.i("MainActivity","savePhotosState");
            mPhotosState = mPhotos.getState();
        }
        if(mTrips!=null&&mTrips.isAdded()){
            Log.i("MainActivity","saveTripsState");
            mTripListState = mTrips.getState();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.i("MainActivity","onSaveInstanceSate");
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
