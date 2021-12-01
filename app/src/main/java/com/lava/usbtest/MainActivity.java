package com.lava.usbtest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Action Bar

        androidx.appcompat.app.ActionBar ab = getSupportActionBar();
//        ab.selectTab(ab.newTab());

        // Fragment Tab
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment mFragment = new USBFragment();
        ft.add(android.R.id.content, mFragment);
        ft.commit();

//        androidx.appcompat.app.ActionBar.Tab tab = ab.newTab().setText(R.string.usb_tab)
//                .setTabListener(new MyTabListener(this, USBFragment.class.getName()));
//        ab.addTab(tab);
//
//        tab = ab.newTab().setText(R.string.msr_tab)
//                .setTabListener(new MyTabListener(this, MSRFragment.class.getName()));
//        ab.addTab(tab);
    }

    // TabListener
    private class MyTabListener implements androidx.appcompat.app.ActionBar.TabListener {
        private static final String TAG = "MyTabListener";
        private androidx.fragment.app.Fragment mFragment;
        private final Activity mActivity;
        private final String mFragName;

        public MyTabListener(Activity activity, String fragName) {
            mActivity = activity;
            mFragName = fragName;
        }


        @Override
        public void onTabSelected(androidx.appcompat.app.ActionBar.Tab tab, androidx.fragment.app.FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mFragName);
                ft.add(android.R.id.content, mFragment);
            } else {
                ft.attach(mFragment);
            }
            Log.d(TAG, "mytab select");
        }

        @Override
        public void onTabUnselected(androidx.appcompat.app.ActionBar.Tab tab, androidx.fragment.app.FragmentTransaction ft) {

            Log.d(TAG, "mytab hide");
            if (mFragment != null)
                ft.detach(mFragment);
        }


        @Override
        public void onTabReselected(androidx.appcompat.app.ActionBar.Tab tab, androidx.fragment.app.FragmentTransaction ft) {

        }
    }
}
