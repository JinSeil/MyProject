package com.treeonesoft.yeogiro;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class ChatLinearLayout extends LinearLayout {
    TextView mTvName;
    TextView mTvBody;
    ImageView mIvProfile;
    LinearLayout mLlFriend;
    Context mContext;

    public ChatLinearLayout(Context context) {
        super(context);
        initView();
        mContext = context;
    }

    public void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;

        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);

        View v = li.inflate(R.layout.chat_item, this, false);
        addView(v);

        mTvName = (TextView) findViewById(R.id.tvName);
        mTvBody = (TextView) findViewById(R.id.tvBody);
        mIvProfile = (ImageView) findViewById(R.id.ivProfileImg);
        mLlFriend = (LinearLayout) findViewById(R.id.llFriend);
    }

    public void setFriendChat(String name, String body, String profileUrl) {
        mLlFriend.setVisibility(View.VISIBLE);
//        mTvName.setVisibility(View.VISIBLE);
        mTvName.setText(name);
        mTvBody.setVisibility(View.VISIBLE);
        mTvBody.setText(body);
        mTvBody.setGravity(Gravity.LEFT);

        if( profileUrl != null && profileUrl.startsWith("http") )
        {
            Log.d("SESIN_AD", "photoUrl = " + profileUrl);
            Glide.with(mContext).load(profileUrl).into(mIvProfile);
        }
        else
        {
            Log.d("SESIN_AD", "photoUrl null");
            mIvProfile.setImageResource(R.drawable.noprofile);
        }
    }

    public void setMyChat(String body) {
//        mTvName.setVisibility(View.GONE);
        mLlFriend.setVisibility(View.GONE);
        mTvBody.setVisibility(View.VISIBLE);
        mTvBody.setText(body);
        mTvBody.setGravity(Gravity.RIGHT);
    }
}
