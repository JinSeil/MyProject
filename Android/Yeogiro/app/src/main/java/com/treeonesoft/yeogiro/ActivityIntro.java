package com.treeonesoft.yeogiro;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class ActivityIntro extends AppCompatActivity {

    private static final String TAG = "SESIN_AI";

    public static final int REQUEST_PERMISSIONS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        String token = FirebaseInstanceId.getInstance().getToken();
        Define.TOKEN = token;

        Log.d(TAG, "hash key : " + Utility.getHashKey(getApplicationContext(), getPackageName()));
    }

    public void getPermission() {
        Log.d(TAG, "getPermission?");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = checkPermissions(getPermissions());
            if (permissions != null && permissions.length > 0)
                requestPermissions(permissions, REQUEST_PERMISSIONS);
            else {
                startMain();
            }
        } else {
            startMain();
        }
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
                        getPermission();
                    } else {
                        getPermission();
                    }
                } else {
                    getPermission();
                }
            }
        });
    }

    ImageView mIvProfile1;
    ImageView mIvProfile2;

    @Override
    public void onStart() {
        super.onStart();

//        mIvProfile1 = findViewById(R.id.ivProfileImg1);
//        mIvProfile2 = findViewById(R.id.ivProfileImg2);
//
//        //https://lh5.googleusercontent.com/-17YAPVK9hXc/AAAAAAAAAAI/AAAAAAAAAAA/ACLGyWD3cMhX5oRKbi7kgo3cACNRLxXvzA/s96-c/photo.jpg
//        //https://lh6.googleusercontent.com/-tqY7ov9w75k/AAAAAAAAAAI/AAAAAAAAAAA/ACLGyWAEgfedOgTwEX5wUw7bRf9ZyNY1cQ/s96-c/photo.jpg
//        String profile1_url = "https://lh5.googleusercontent.com/-17YAPVK9hXc/AAAAAAAAAAI/AAAAAAAAAAA/ACLGyWD3cMhX5oRKbi7kgo3cACNRLxXvzA/s96-c/photo.jpg";
//        String profile2_url = "https://lh6.googleusercontent.com/-tqY7ov9w75k/AAAAAAAAAAI/AAAAAAAAAAA/ACLGyWAEgfedOgTwEX5wUw7bRf9ZyNY1cQ/s96-c/photo.jpg";
//
//        Glide.with(this).load(profile1_url).into(mIvProfile1);
//        Glide.with(this).load(profile2_url).into(mIvProfile2);

//        Intent intent = new Intent(getApplicationContext(), ActivityLogin2.class);
//        startActivity(intent);
//
//        finish();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "currentUser " + currentUser);
        if (currentUser != null)
            getLoginUser(currentUser);
        else
            getPermission();
    }

    public void startMain() {
        if (Define.LOGIN_USER != null) {
            Intent intent = new Intent(getApplicationContext(), ActivityFriendMain.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean isGranted = true;

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    isGranted = false;
            }

            if (!isGranted) {
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                startMain();
            }
        }
    }

    public String[] checkPermissions(String[] permissions) {
        ArrayList<String> list = new ArrayList<String>();

        String[] result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }
        }

        result = new String[list.size()];
        result = list.toArray(result);
        return result;
    }

    public String[] getPermissions() {
        String[] result = null;

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(
                    getPackageName().toString(),
                    PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        result = packageInfo.requestedPermissions;

        return result;
    }
}
