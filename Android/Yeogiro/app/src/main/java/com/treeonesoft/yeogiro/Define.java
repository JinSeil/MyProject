package com.treeonesoft.yeogiro;

public class Define {

    public static String TOKEN = null;

    public static final String LOGIN_EXCEPTION_NO_USER = "FirebaseAuthInvalidUserException";
    public static final String LOGIN_EXCEPTION_WRONG_PASSWORD = "FirebaseAuthInvalidCredentialsException";
    public static final String LOGIN_EXCEPTION_UNKNOWN = "FirebaseLoginUnKnownException";

    public static final String CREATE_ACCOUNT_EXCETPION_USE_EMAIL = "FirebaseAuthUserCollisionException";
    public static final String CREATE_ACCOUNT_EXCEPTION_UNKNOWN = "FirebaseCreateUnKnownException";

    public static final String FB_DB_COLLECTION_USERS = "Users";

    public static final String ACTION_UI_CHANGE = "com.jinseil.testfirebase.uichange";

    public static User LOGIN_USER;

    public static final boolean DEBUG = false;

}
