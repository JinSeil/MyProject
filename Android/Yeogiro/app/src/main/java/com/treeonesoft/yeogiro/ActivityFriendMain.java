package com.treeonesoft.yeogiro;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

public class ActivityFriendMain extends AppCompatActivity {
    private static final String TAG = "SESIN_AFM";

    private static final int POSITION_FRIEND = 0;
    private static final int POSITION_FRIEND_FIND = 1;
    private static final int POSITION_SETTINGS = 2;

    TabLayout mTabLayout;

    Fragment mFragment;

    android.support.v4.app.FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_main);

        mFragmentManager = getSupportFragmentManager();

        mTabLayout = findViewById(R.id.tabLayout);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.friend));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.friend_find));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.settings));

        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mFragment = new FragmentFriend();
        mFragmentManager.beginTransaction().add(R.id.content_main, mFragment).commit();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected " + tab.getPosition());
                switch (tab.getPosition()) {
                    case 0: {
                        mFragment = new FragmentFriend();
                        mFragmentManager.beginTransaction().replace(R.id.content_main, mFragment).commit();
                        break;
                    }
                    case 1: {
                        mFragment = new FragmentFriendFind();
                        mFragmentManager.beginTransaction().replace(R.id.content_main, mFragment).commit();
                        break;
                    }
                    case 2: {
                        mFragment = new FragmentSettings();
                        mFragmentManager.beginTransaction().replace(R.id.content_main, mFragment).commit();
                        break;
                    }

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabUnselected " + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected " + tab.getPosition());

            }
        });

        registerUIReceiver();
    }

    public void onClick(View view) {
        int resId = view.getId();

        if (resId == R.id.btLogout) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null)
                        Log.d(TAG, "user is not null");
                    else
                        Log.d(TAG, "user is null");
                }
            });
            FirebaseAuth.getInstance().signOut();

            Define.LOGIN_USER = null;

            Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterUIReceiver();
    }

    BroadcastReceiver mUIReceiver;

    public void registerUIReceiver() {
        if (mUIReceiver == null) {
            mUIReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Define.ACTION_UI_CHANGE.equalsIgnoreCase(action)) {
                        switch (mTabLayout.getSelectedTabPosition()) {
                            case POSITION_FRIEND: {
                                ((FragmentFriend) mFragment).refreshList(new RefreshListener() {
                                    @Override
                                    public void onRefreshSucess(boolean isSucess) {

                                    }
                                });
                                break;
                            }
                            case POSITION_FRIEND_FIND: {
                                ((FragmentFriendFind) mFragment).refreshList(new RefreshListener() {
                                    @Override
                                    public void onRefreshSucess(boolean isSucess) {

                                    }
                                });
                                break;
                            }
                            case POSITION_SETTINGS: {
                                break;
                            }
                        }
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Define.ACTION_UI_CHANGE);

            this.registerReceiver(mUIReceiver, intentFilter);
        }
    }

    public void unregisterUIReceiver() {
        if (mUIReceiver != null) {
            this.unregisterReceiver(mUIReceiver);
            mUIReceiver = null;
        }
    }
}
