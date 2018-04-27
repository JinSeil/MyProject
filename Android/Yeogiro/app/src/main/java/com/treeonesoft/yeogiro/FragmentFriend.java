package com.treeonesoft.yeogiro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FragmentFriend extends Fragment {

    private static final String TAG = "SESIN_FF";

    ListView mListView;
    FriendsArrayAdapter mAdapter;
    ArrayList<User> mList;
    Context mContext;
    Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend, parentViewGroup, false);
        Log.d(TAG, "onCreateView");
        mListView = rootView.findViewById(R.id.lvList);
        getData();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach context");
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach activity");
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void refreshList(RefreshListener listener) {
        getData();
    }

    public void getData() {
        showProgress();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (mList == null)
                        mList = new ArrayList<User>();
                    else
                        mList.clear();

                    User currentUser = null;
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d("SESIN", document.getId() + "=>" + document.getData());
                        User user = document.toObject(User.class);
                        if (!document.getId().equalsIgnoreCase(Define.LOGIN_USER.getUid())) {
//                            mList.add(user);
                            ArrayList<String> myList = Define.LOGIN_USER.getFriends();
                            ArrayList<String> friendList = user.getFriends();

                            if (myList != null && friendList != null && myList.size() > 0 && friendList.size() > 0) {
                                boolean isMyF = false;
                                boolean isFriendF = false;

                                for (int i = 0; i < myList.size(); i++) {
                                    if (myList.get(i).equalsIgnoreCase(user.getUid())) {
                                        isMyF = true;
                                        break;
                                    }
                                }

                                for (int i = 0; i < friendList.size(); i++) {
                                    if (friendList.get(i).equalsIgnoreCase(Define.LOGIN_USER.getUid())) {
                                        isFriendF = true;
                                        break;
                                    }
                                }

                                if (isMyF && isFriendF)
                                    mList.add(user);
                            }
                        } else {
                            Define.LOGIN_USER = currentUser = user;
                        }
                    }

                    if (mList.size() > 0) {

                        mListView.setVisibility(View.VISIBLE);
                        getView().findViewById(R.id.tvEmpty).setVisibility(View.GONE);

                        if (mContext != null)
                            mAdapter = new FriendsArrayAdapter(mContext, mList, currentUser, new FriendsArrayAdapter.FriendBtListener() {
                                @Override
                                public void onBtClick() {
                                    refreshList(null);
                                }
                            });
                        else if (mActivity != null)
                            mAdapter = new FriendsArrayAdapter(mContext, mList, currentUser, new FriendsArrayAdapter.FriendBtListener() {
                                @Override
                                public void onBtClick() {
                                    refreshList(null);
                                }
                            });

                        mListView.setAdapter(mAdapter);
                    } else {
                        mListView.setVisibility(View.GONE);
                        getView().findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
                    }
                    closeProgress();
                } else {
                    closeProgress();
                }
            }
        });
    }


    ProgressDialog mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null) {
            if (mActivity != null)
                mProgressDialog = new ProgressDialog(mActivity);
            else if (mContext != null)
                mProgressDialog = new ProgressDialog(mContext);

            mProgressDialog.setMessage("로딩중입니다.");

            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);

            mProgressDialog.show();
        }
    }

    public void closeProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
    }
}
