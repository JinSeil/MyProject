package com.treeonesoft.yeogiro.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.treeonesoft.yeogiro.Define;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private static final String TAG = "SESIN";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token)
    {
        Log.d(TAG, "IDService!");
        Define.TOKEN = token;
    }
}
