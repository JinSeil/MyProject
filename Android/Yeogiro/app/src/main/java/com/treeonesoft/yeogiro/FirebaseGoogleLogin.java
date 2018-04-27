package com.treeonesoft.yeogiro;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FirebaseGoogleLogin {
    Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    public FirebaseGoogleLogin(Context context) {
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

    public void logout() {
        if (isLogin()) {
            FirebaseAuth.getInstance().signOut();
            mCurrentUser = null;
        }
    }

    public void googleLogin(Activity activity, GoogleSignInAccount acct, @NonNull final LoginListener listener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mCurrentUser = mAuth.getCurrentUser();
                            listener.loginResult(true, mCurrentUser, null);
                        } else {
                            String exception = task.getException().getMessage();
                            listener.loginResult(false, mCurrentUser, exception);
                        }
                    }
                });
    }
}
