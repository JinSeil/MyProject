package com.treeonesoft.yeogiro;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class FCMProtocol {

    public static final String DATA_TYPE = "dataType";
    public static final String REQUEST_TYPE = "requestType";
    public static final String MESSAGE = "message";
    public static final String SENDER_TOKEN = "senderToken";
    public static final String SENDER_NAME = "senderName";
    public static final String SENDER_UID = "senderUid";
    public static final String RECEIVER_TOKEN = "receiverToken";

    public enum DataType {
        FRIEND_ME(0), FRIEND_ME_CANCLE(1), FRIEND_YOU(2), FRIEND_YOU_CANCLE(3), FRIEND_DELETE(4), WALK_ME(5), LOCATION(6),
        CHAT(7), TARGET_PLACE(8), TARGET_MF(9), TARGET_PLACE_A(10), TARGET_MF_A(13), LOCATION_WANT(14), DISCONNECT(15);

        private int mValue;

        private DataType(int value)
        {
            mValue = value;
        }

        public int getValue(){
            return mValue;
        }
    }

    public enum RequestType
    {
        QUESTION(0), ANSWER(1);

        private int mValue;

        private RequestType(int value)
        {
            mValue = value;
        }

        public int getValue(){
            return mValue;
        }
    }

    public FirebaseFunctions mFunction;

    public void sendMessage(DataType type, RequestType requestType, String message, String senderToken, String senderName, String senderUid, String receiverToken)
    {
        if( mFunction == null )
            mFunction = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put(FCMProtocol.DATA_TYPE, type);
        data.put(FCMProtocol.REQUEST_TYPE, requestType);
        data.put(FCMProtocol.MESSAGE, message);
        data.put(FCMProtocol.SENDER_TOKEN, senderToken);
        data.put(FCMProtocol.SENDER_NAME, senderName);
        data.put(FCMProtocol.SENDER_UID, senderUid);
        data.put(FCMProtocol.RECEIVER_TOKEN, receiverToken);

        Log.d("SESIN", type + " " + requestType + " " + message + " " + senderToken + " " + senderName + " " + senderUid + " " + receiverToken);

        mFunction.getHttpsCallable("sendNotification")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = (String) task.getResult().getData();
                        Log.d("SESIN", "result : " + result);
                        return result;
                    }
                });
    }
}
