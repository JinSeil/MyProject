package com.treeonesoft.yeogiro;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

public class ActivityLocationDialog extends AppCompatActivity {

    private static final String TAG = "SESIN_ALD";

    public static final String DIALOG_MODE = "dialog_mode";

    public static final int DIALOG_MODE_QUESTION = 0;
    public static final int DIALOG_MODE_ANSWER = 1;

    int mDialogMode = DIALOG_MODE_QUESTION;

    String mRoomId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "currentUser " + currentUser);
        if (currentUser != null)
            getLoginUser(currentUser);
        else
            finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterUiReceiver();
    }

    public void getLoginUser(FirebaseUser currentUser) {
        Log.d(TAG, "getLoginUser?");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Define.LOGIN_USER = document.toObject(User.class);
                        setDialog();
                    } else {
                        setDialog();
                    }
                } else {
                    setDialog();
                }
            }
        });
    }

    public void setDialog() {
        mDialogMode = getIntent().getIntExtra(DIALOG_MODE, -1);

        if (mDialogMode == -1) {
            finish();
            return;
        }

        String jsonData = getIntent().getStringExtra("data");

        Log.d(TAG, mDialogMode + " jsonData=" + jsonData);

        if (mDialogMode == DIALOG_MODE_QUESTION) {
            User friendUser = Utility.jsonToUser(jsonData);
            showLocationDialog(friendUser);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String dataType = jsonObject.getString(FCMProtocol.DATA_TYPE);
                String reqType = jsonObject.getString(FCMProtocol.REQUEST_TYPE);
                String message = jsonObject.getString(FCMProtocol.MESSAGE);
                String senderToken = jsonObject.getString(FCMProtocol.SENDER_TOKEN);
                String senderName = jsonObject.getString(FCMProtocol.SENDER_NAME);
                String senderUid = jsonObject.getString(FCMProtocol.SENDER_UID);
                String receiverToken = jsonObject.getString(FCMProtocol.RECEIVER_TOKEN);

                showQuestionDialog(senderName, senderUid, senderToken, null);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "exception=" + e.getMessage());
            }
        }
//        Toast.makeText(getApplicationContext(), "dialog mode = " + mDialogMode + "\n" + jsonData, Toast.LENGTH_SHORT).show();

        registerUiReceiver();
    }

    public void showLocationDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치정보요청");
        builder.setMessage(user.getDisplayName() + "에게 위치정보를 요청하시겠습니까?");
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utility.sendMessage(FCMProtocol.DataType.LOCATION, FCMProtocol.RequestType.QUESTION, "test", Define.LOGIN_USER.getToken(),
                        Define.LOGIN_USER.getDisplayName(), Define.LOGIN_USER.getUid(), user.getToken());
                dialog.cancel();
                dialog.dismiss();
//                Toast.makeText(getApplicationContext(), "위치정보를 요청했습니다!", Toast.LENGTH_SHORT).show();
                mRoomId = Define.LOGIN_USER.getUid();
                showProgress();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    public void showQuestionDialog(String senderName, final String senderUid, final String senderToken, LatLng senderLoc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치정보요청");
        builder.setMessage(senderName + "에게 위치정보를 요청받았습니다. 위치정보를 전송하시겠습니까?");
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utility.sendMessage(FCMProtocol.DataType.LOCATION, FCMProtocol.RequestType.ANSWER, "Y", Define.LOGIN_USER.getToken(),
                        Define.LOGIN_USER.getDisplayName(), Define.LOGIN_USER.getUid(), senderToken);
                dialog.cancel();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "위치정보를 전송했습니다!", Toast.LENGTH_SHORT).show();
                mRoomId = senderUid;

                startMapActivity();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
                Utility.sendMessage(FCMProtocol.DataType.LOCATION, FCMProtocol.RequestType.ANSWER, "N", Define.LOGIN_USER.getToken(),
                        Define.LOGIN_USER.getDisplayName(), Define.LOGIN_USER.getUid(), senderToken);
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    BroadcastReceiver mUiReceiver;

    public void registerUiReceiver() {
        if (mUiReceiver == null) {
            mUiReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (Define.ACTION_UI_CHANGE.equalsIgnoreCase(action)) {
                        String result = intent.getStringExtra("data");
                        if (result != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String dataType = jsonObject.getString(FCMProtocol.DATA_TYPE);
                                String reqType = jsonObject.getString(FCMProtocol.REQUEST_TYPE);
                                String message = jsonObject.getString(FCMProtocol.MESSAGE);
                                String senderToken = jsonObject.getString(FCMProtocol.SENDER_TOKEN);
                                String senderName = jsonObject.getString(FCMProtocol.SENDER_NAME);
                                String senderUid = jsonObject.getString(FCMProtocol.SENDER_UID);
                                String receiverToken = jsonObject.getString(FCMProtocol.RECEIVER_TOKEN);

                                if (dataType.equalsIgnoreCase("LOCATION")) {
                                    if (reqType.equalsIgnoreCase("QUESTION")) {

                                    } else if (reqType.equalsIgnoreCase("ANSWER")) {
                                        closeProgress();
                                        if (message.equalsIgnoreCase("Y")) {
                                            startMapActivity();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "상대방이 거절 했습니다!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "exception=" + e.getMessage());
                            }
                        }
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Define.ACTION_UI_CHANGE);

            this.registerReceiver(mUiReceiver, intentFilter);
        }
    }

    public void unregisterUiReceiver() {
        if (mUiReceiver != null) {
            this.unregisterReceiver(mUiReceiver);
            mUiReceiver = null;
        }
    }

    public void startMapActivity() {
        Intent intent = new Intent(getApplicationContext(), ActivityMap.class);
        intent.putExtra("roomId", mRoomId);
        startActivity(intent);
        finish();
    }

    ProgressDialog mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);

            mProgressDialog.setMessage("상대방에게 요청중 입니다.");

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
