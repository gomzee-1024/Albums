package com.utils.gdkcorp.albums.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utils.gdkcorp.albums.fragments.Login;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.fragments.SignUp;

public class LogInSignUpActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;
    private Login signInFragment;
    private SignUp signUpFragment;
    private static final int EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private DatabaseReference mConnectedRef;
    private ValueEventListener mConnectedListener;
    private FrameLayout mRootLayout;
    private boolean mConnected=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up);
        systemUITweek();
        checkExternalStoragePermission();
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

    private void checkExternalStoragePermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                    PackageManager.PERMISSION_GRANTED
                    &&ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                    initUI();
                    checkConnection();
            }else{
//                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
//                    //Toast.makeText(this,"App. needs this permission to show photos",Toast.LENGTH_SHORT).show();
//                }
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        }else{
            initUI();
            checkConnection();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_RESULT:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permissions Granted",Toast.LENGTH_SHORT).show();
                    initUI();
                }else{
                    Toast.makeText(this,"App. needs these permissions to work",Toast.LENGTH_LONG).show();
                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            EXTERNAL_STORAGE_PERMISSION_RESULT);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void initUI() {
        mConnectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        mRootLayout = (FrameLayout) findViewById(R.id.login_sign_up_root);
        mTabLayout = (TabLayout) findViewById(R.id.log_in_signup_tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.log_in_signup_view_pager);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        signInFragment = Login.newInstance("","");
        signUpFragment = SignUp.newInstance("","");
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void systemUITweek() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                return signInFragment;
            }else{
                return signUpFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0){
                return "Sign In";
            }else{
                return "Sign Up";
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(mConnectedListener!=null)
        mConnectedRef.removeEventListener(mConnectedListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
