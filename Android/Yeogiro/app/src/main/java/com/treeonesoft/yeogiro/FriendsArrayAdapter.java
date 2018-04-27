package com.treeonesoft.yeogiro;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

class FriendsArrayAdapter extends BaseAdapter {
    interface FriendBtListener {
        public void onBtClick();
    }

    FriendBtListener mListener;

    Context mContext;
    ArrayList<User> mList;
    int mItemResourceId;
    User mLoginUser;

    public FriendsArrayAdapter(Context context, ArrayList<User> list, User loginUser, FriendBtListener listener) {
        mContext = context;
        mList = list;
        mItemResourceId = R.layout.friend_item;
        mLoginUser = loginUser;
        mListener = listener;
    }

    public void setLoginUser(User loginUser) {
        mLoginUser = loginUser;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    public void setItem(int position, User user) {
        mList.set(position, user);
        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            final Context context = parent.getContext();

            LayoutInflater inflater = null;

            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            convertView = inflater.inflate(mItemResourceId, parent, false);
        }

        final User user = mList.get(position);

        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvEmail = (TextView) convertView.findViewById(R.id.tvEmail);
        TextView tvFriend = (TextView) convertView.findViewById(R.id.tvFirend);

        ImageView ivProfile = convertView.findViewById(R.id.ivProfileImg);

        TextView btWhere = convertView.findViewById(R.id.tvWhere);
        TextView btOk = convertView.findViewById(R.id.tvOk);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyLoginUser(user);
                FCMProtocol.RequestType reqType = FCMProtocol.RequestType.QUESTION;
                Utility.sendMessage(user.mDataType, reqType, "test", mLoginUser.getToken(), mLoginUser.getDisplayName(), mLoginUser.getUid(), user.getToken());
                if (mListener != null)
                    mListener.onBtClick();
            }
        });
        btWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ActivityLocationDialog.class);
                intent.putExtra(ActivityLocationDialog.DIALOG_MODE, ActivityLocationDialog.DIALOG_MODE_QUESTION);
                intent.putExtra("data", Utility.userToJsonString(user));
                mContext.startActivity(intent);

            }
        });

        if (user != null) {
            tvName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());

            if( user.getPhotoUrl() != null && user.getPhotoUrl().startsWith("http") )
            {
                Log.d("SESIN_AD", "photoUrl = " + user.getPhotoUrl());
                Glide.with(mContext).load(user.getPhotoUrl()).into(ivProfile);
            }
            else
            {
                Log.d("SESIN_AD", "photoUrl null");
                ivProfile.setImageResource(R.drawable.noprofile);
            }

            ArrayList<String> listFriends = user.getFriends();
            ArrayList<String> myFriends = mLoginUser.getFriends();

            if (myFriends == null && listFriends == null) {
                tvFriend.setText("친구아님!");
                btOk.setVisibility(View.VISIBLE);
//                btOk.setText("친구\n요청");
                btOk.setBackgroundResource(R.drawable.ic_user_add);
                user.mDataType = FCMProtocol.DataType.FRIEND_ME;
                btWhere.setVisibility(View.GONE);
            } else if (myFriends == null && listFriends != null) {
                boolean isMyFriend = false;

                for (int i = 0; i < listFriends.size(); i++) {
                    if (mLoginUser.getUid().equalsIgnoreCase(listFriends.get(i))) {
                        user.isWantMeFriend = isMyFriend = true;
                        break;
                    }
                }

                if (isMyFriend) {
                    tvFriend.setText("상대방이 친구 신청을 했습니다.");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("수락");
                    btOk.setBackgroundResource(R.drawable.ic_user_check);
                    user.mDataType = FCMProtocol.DataType.FRIEND_YOU;
                    btWhere.setVisibility(View.GONE);
                } else {
                    tvFriend.setText("친구아님!");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("친구\n요청");
                    btOk.setBackgroundResource(R.drawable.ic_user_add);
                    user.mDataType = FCMProtocol.DataType.FRIEND_ME;
                    btWhere.setVisibility(View.GONE);
                }
            } else if (myFriends != null && listFriends == null) {
                boolean isWantFriend = false;

                for (int i = 0; i < myFriends.size(); i++) {
                    Log.d("SESIN", user.getUid() + "\n" + myFriends.get(i));
                    if (user.getUid().equalsIgnoreCase(myFriends.get(i))) {
                        user.isWantYouFriend = isWantFriend = true;
                        break;
                    }
                }

                Log.d("SESIN", "isWantFriend : " + isWantFriend);

                if (!isWantFriend) {
                    tvFriend.setText("친구아님!");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("친구\n요청");
                    btOk.setBackgroundResource(R.drawable.ic_user_add);
                    user.mDataType = FCMProtocol.DataType.FRIEND_ME;
                    btWhere.setVisibility(View.GONE);
                } else {
                    tvFriend.setText("친구 신청중...");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("신청\n취소");
                    btOk.setBackgroundResource(R.drawable.ic_user_subtract);
                    user.mDataType = FCMProtocol.DataType.FRIEND_ME_CANCLE;
                    btWhere.setVisibility(View.GONE);
                }
            } else {
                boolean isMyFriend = false;
                boolean isWantFriend = false;

                for (int i = 0; i < listFriends.size(); i++) {
                    if (mLoginUser.getUid().equalsIgnoreCase(listFriends.get(i))) {
                        user.isWantMeFriend = isMyFriend = true;
                        break;
                    }
                }

                for (int i = 0; i < myFriends.size(); i++) {
                    if (user.getUid().equalsIgnoreCase(myFriends.get(i))) {
                        user.isWantYouFriend = isWantFriend = true;
                        break;
                    }
                }

                if (isMyFriend && isWantFriend) {
                    tvFriend.setText("친구임!");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("친구\n삭제");
                    btOk.setBackgroundResource(R.drawable.ic_user_subtract);
                    user.mDataType = FCMProtocol.DataType.FRIEND_DELETE;
                    btWhere.setVisibility(View.VISIBLE);
                } else if (isMyFriend && !isWantFriend) {
                    tvFriend.setText("상대방이 친구 신청을 했습니다.");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("수락");
                    btOk.setBackgroundResource(R.drawable.ic_user_check);
                    user.mDataType = FCMProtocol.DataType.FRIEND_YOU;
                    btWhere.setVisibility(View.GONE);
                } else if (!isMyFriend && isWantFriend) {
                    tvFriend.setText("친구 신청중...");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("신청\n취소");
                    btOk.setBackgroundResource(R.drawable.ic_user_subtract);
                    user.mDataType = FCMProtocol.DataType.FRIEND_ME_CANCLE;
                    btWhere.setVisibility(View.GONE);
                } else {
                    tvFriend.setText("친구아님!");
                    btOk.setVisibility(View.VISIBLE);
//                    btOk.setText("친구\n요청");
                    btOk.setBackgroundResource(R.drawable.ic_user_add);
                    user.mDataType = FCMProtocol.DataType.FRIEND_ME;
                    btWhere.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }

    public void modifyLoginUser(User friendUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        switch (friendUser.mDataType) {
            case FRIEND_ME: {
                mLoginUser.addFriend(friendUser.getUid());
                break;
            }
            case FRIEND_ME_CANCLE: {
                mLoginUser.removeFriend(friendUser.getUid());
                break;
            }
            case FRIEND_YOU: {
                mLoginUser.addFriend(friendUser.getUid());
                break;
            }
            case FRIEND_YOU_CANCLE: {
                mLoginUser.removeFriend(friendUser.getUid());
                break;
            }
            case FRIEND_DELETE: {
                mLoginUser.removeFriend(friendUser.getUid());
                break;
            }
        }

        db.collection("Users").document(mLoginUser.getUid()).set(mLoginUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FriendsArrayAdapter.this.notifyDataSetChanged();
                Define.LOGIN_USER = mLoginUser;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
