package com.utils.gdkcorp.albums.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.utils.gdkcorp.albums.fragments.Login;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.fragments.SignUp;

public class LogInSignUpActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;
    private Login signInFragment;
    private SignUp signUpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up);
        systemUITweek();
        initUI();
    }

    private void initUI() {
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
}
