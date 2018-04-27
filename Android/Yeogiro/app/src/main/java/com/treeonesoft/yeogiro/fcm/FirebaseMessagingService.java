package com.treeonesoft.yeogiro.fcm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.treeonesoft.yeogiro.ActivityLocationDialog;
import com.treeonesoft.yeogiro.Define;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static String TAG = "SESIN";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0)
        {
            if( remoteMessage.getData().get("dataType").equalsIgnoreCase("LOCATION") )
            {
                if( remoteMessage.getData().get("requestType").equalsIgnoreCase("QUESTION") )
                {
                    startActivity(getApplicationContext(), ActivityLocationDialog.class, ActivityLocationDialog.DIALOG_MODE_ANSWER, getJsonStringFromMap(remoteMessage.getData()).toString());
                }
                else
                {
                    Intent intent = new Intent();
                    intent.setAction(Define.ACTION_UI_CHANGE);
                    intent.putExtra("data", getJsonStringFromMap(remoteMessage.getData()).toString());
                    sendBroadcast(intent);
                }
            }
            else
            {
                Intent intent = new Intent();
                intent.setAction(Define.ACTION_UI_CHANGE);
                intent.putExtra("data", getJsonStringFromMap(remoteMessage.getData()).toString());
                sendBroadcast(intent);
            }
            Log.d(TAG, "data playoad " + remoteMessage.getData());
            Log.d(TAG, "data dataType=" + remoteMessage.getData().get("dataType"));
            Log.d(TAG, "data requestType=" + remoteMessage.getData().get("requestType"));

        }

        if( remoteMessage.getNotification() != null )
        {
            Log.d(TAG, "Noti body " + remoteMessage.getNotification().getBody());
        }
    }

    public static JSONObject getJsonStringFromMap( Map<String, String> map ) {

        JSONObject json = new JSONObject();
        for( Map.Entry<String, String> entry : map.entrySet() ) {
            String key = entry.getKey();
            Object value = entry.getValue();
            try {
                json.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return json;
    }

    public static void startActivity(Context context, Class activityClass, int mode, String jsondata)
    {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ActivityLocationDialog.DIALOG_MODE, mode);
        intent.putExtra("data", jsondata);
        context.startActivity(intent);
    }
}
