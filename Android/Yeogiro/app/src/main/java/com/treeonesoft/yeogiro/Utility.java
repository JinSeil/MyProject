package com.treeonesoft.yeogiro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    /**
     * 암호 유효성 검사 (6자리 이상 한글 미포함)
     * @param target
     * @return boolean
     */
    public static boolean isValidPassword(String target)
    {
        Pattern p = Pattern.compile("(^.*(?=.{6,100})(?=.*[0-9])(?=.*[a-zA-Z]).*$)");

        Matcher m = p.matcher(target);
        if (m.find() && !target.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 이메일 유효성 검사
     * @param target
     * @return boolean
     */
    public static boolean isValidEmail(String target)
    {
        if (target == null || TextUtils.isEmpty(target))
            return false;
        else
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();

    }

//    public static String getKeyHash(final Context context) {
//        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
//        if (packageInfo == null)
//            return null;
//
//        for (Signature signature : packageInfo.signatures) {
//            try {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                return android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
//            } catch (NoSuchAlgorithmException e) {
//                Log.w("===", "키 해시 : " + signature, e);
//            }
//        }
//        return null;
//    }

    public static JSONObject getJsonStringFromMap(Map<String, String> map ) {

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

    public static void sendMessage(FCMProtocol.DataType type, FCMProtocol.RequestType requestType, String message, String senderToken, String senderName, String senderUid, String receiverToken)
    {
//        if( mFunction == null )
        FirebaseFunctions mFunction = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put(FCMProtocol.DATA_TYPE, type.toString());
        data.put(FCMProtocol.REQUEST_TYPE, requestType.toString());
        data.put(FCMProtocol.MESSAGE, message);
        data.put(FCMProtocol.SENDER_TOKEN, senderToken);
        data.put(FCMProtocol.SENDER_NAME, senderName);
        data.put(FCMProtocol.SENDER_UID, senderUid);
        data.put(FCMProtocol.RECEIVER_TOKEN, receiverToken);

        Log.d("SESIN", type.toString() + " " + requestType.toString() + " " + message + " " + senderToken + " " + senderName + " " + senderUid + " " + receiverToken);

        mFunction.getHttpsCallable("sendNotification2")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = ((Map<String, Object>) task.getResult().getData()).toString();
                        Log.d("SESIN", "result : " + result);
                        return result;
                    }
                }).addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if( !task.isSuccessful() )
                {
                    Log.d("SESIN", "exception : " + task.getException().toString());
                    Exception e = task.getException();
                    e.printStackTrace();
                    if( e instanceof FirebaseFunctionsException)
                    {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();

                        Log.d("SESIN", "code:" + code.toString());
//                        Log.d("SESIN", "deatil:" + details.toString());
                    }
                }
            }
        });
    }

    public static String userToJsonString(User user)
    {
        /*

        private String email;
        private String displayName;
        private String phoneNumber;
        private String providerId;
        private String photoUrl;
        private String token;
        private String uid;
        private ArrayList<String> friends;

        */
//        String result = null;

//        JSONObject obj = new JSONObject();

        JSONObject profileObj = new JSONObject();

        JSONArray friendObj = new JSONArray();

        try {
            profileObj.put("email", user.getEmail() == null ? " " : user.getEmail());
            profileObj.put("displayName", user.getDisplayName() == null ? " " : user.getDisplayName());
            profileObj.put("phoneNumber", user.getPhoneNumber() == null ? " " : user.getPhoneNumber());
            profileObj.put("providerId", user.getProviderId() == null ? " " : user.getProviderId());
            profileObj.put("photoUrl", user.getPhotoUrl() == null ? " " : user.getPhotoUrl());
            profileObj.put("token", user.getToken() == null ? " " : user.getToken());
            profileObj.put("uid", user.getUid() == null ? " " : user.getUid());

            if( user.getFriends() != null && user.getFriends().size() > 0 )
            {
                for( int i = 0 ; i < user.getFriends().size() ; i++ )
                {
                    friendObj.put(user.getFriends().get(i));
                }
            }

            profileObj.put("friends", friendObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return profileObj.toString();
    }

    public static User jsonToUser(String jsonString)
    {
        User user = null;

        try {
            JSONObject obj = new JSONObject(jsonString);
            user = new User();
            user.setEmail(obj.getString("email"));
            user.setDisplayName(obj.getString("displayName"));
            user.setPhoneNumber(obj.getString("phoneNumber"));
            user.setProviderId(obj.getString("providerId"));
            user.setPhotoUrl(obj.getString("photoUrl"));
            user.setToken(obj.getString("token"));
            user.setUid(obj.getString("uid"));

            ArrayList<String> friends = new ArrayList<String>();

            JSONArray jsonArray = obj.getJSONArray("friends");
            if( jsonArray != null && jsonArray.length() > 0 )
            {
                for( int i = 0 ; i < jsonArray.length() ; i++ )
                {
                    friends.add((String)jsonArray.get(i));
                }
            }

            user.setFriends(friends);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    @SuppressLint("MissingPermission")
    public static String getPhoneNumber(Context context)
    {
        String phoneNumber = null;

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            phoneNumber = tm.getLine1Number();
            phoneNumber = phoneNumber.replace("+82", "0");
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        return phoneNumber;
    }

    public static Bitmap tintImage(Bitmap bitmap, int color)
    {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
    }

    public static String getHashKey(Context context, String pkgName)
    {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("===", "키 해시 : " + signature, e);
            }
        }
        return null;

    }
}
