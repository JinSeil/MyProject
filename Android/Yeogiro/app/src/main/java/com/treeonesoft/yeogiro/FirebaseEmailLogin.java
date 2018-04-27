package com.treeonesoft.yeogiro;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseEmailLogin {

    private static final String TAG = "SESIN_EMAIL_LOGIN";

    private Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    public FirebaseEmailLogin(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isLogin() {
        mCurrentUser = mAuth.getCurrentUser();
        if (mCurrentUser != null)
            return true;
        else
            return false;
    }

    public FirebaseUser getCurrentUser() {
        return mCurrentUser;
    }

    public void createLoginEmail(Activity activity, String email, String pwd, @NonNull final LoginListener listener) {
        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mCurrentUser = mAuth.getCurrentUser();
                            Log.d(TAG, "create User sucess!");
                            listener.loginResult(true, mCurrentUser, null);
                        } else {
                            Log.d(TAG, "create User fail! " + task.getException());
                            String exception = Define.CREATE_ACCOUNT_EXCEPTION_UNKNOWN;

                            if (task.getException().getClass().equals(FirebaseAuthUserCollisionException.class))
                                exception = Define.CREATE_ACCOUNT_EXCETPION_USE_EMAIL;

                            listener.loginResult(false, mCurrentUser, exception);
                        }
                    }
                });
    }

    public void signLoginEmail(Activity activity, String email, String pwd, @NonNull final LoginListener listener) {
        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mCurrentUser = mAuth.getCurrentUser();
                            Log.d(TAG, "Login sucess!");
                            listener.loginResult(true, mCurrentUser, null);
                        } else {
                            Log.d(TAG, "Login fail! " + task.getException());
                            String exception = Define.LOGIN_EXCEPTION_UNKNOWN;

                            if (task.getException().getClass().equals(FirebaseAuthInvalidUserException.class))
                                exception = Define.LOGIN_EXCEPTION_NO_USER;
                            else if (task.getException().getClass().equals(FirebaseAuthInvalidCredentialsException.class))
                                exception = Define.LOGIN_EXCEPTION_WRONG_PASSWORD;

                            listener.loginResult(false, mCurrentUser, exception);
                        }
                    }
                });
    }

    public void logout() {
        if (isLogin()) {
            FirebaseAuth.getInstance().signOut();
            mCurrentUser = null;
        }
    }

}
