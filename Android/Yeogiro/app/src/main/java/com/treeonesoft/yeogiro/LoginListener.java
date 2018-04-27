package com.treeonesoft.yeogiro;

import com.google.firebase.auth.FirebaseUser;

public interface LoginListener {
    public void loginResult(boolean isResult, FirebaseUser currentUser, String exception);
}
