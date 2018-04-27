package com.treeonesoft.yeogiro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SESIN_AL";

    private static final int RC_SIGN_IN = 123;

    EditText mEtId;
    EditText mEtPwd;

    FirebaseEmailLogin mEmailLogin;
    FirebaseGoogleLogin mGoogleLogin;

    FirebaseUser mCurrentUser;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setView();

        setGoogleSignInClient();
    }

    public void setGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
//            IdpResponse response = IdpResponse.fromResultIntent(data);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            Log.d(TAG, "reqCode " + requestCode + " " + resultCode + " " + result.isSuccess());

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (mGoogleLogin == null)
                    mGoogleLogin = new FirebaseGoogleLogin(this);
                mGoogleLogin.googleLogin(this, account, new LoginListener() {
                    @Override
                    public void loginResult(boolean isResult, FirebaseUser currentUser, String exception) {
                        mCurrentUser = currentUser;
                        showProgress();
                        updateUI(isResult);
                    }
                });
            } else {

            }
        }
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R.id.btAdd) {
//            addData();
        } else if (resId == R.id.btRead) {
//            readData();
        } else if (resId == R.id.btLogin) {
//            setLogin();
            setEmailLogin(false);
        } else if (resId == R.id.btCreateAccount) {
            setEmailLogin(true);
        } else if (resId == R.id.btLogout || resId == R.id.btLogout2) {
            if (mEmailLogin == null)
                mEmailLogin = new FirebaseEmailLogin(this);

            if (mEmailLogin.isLogin()) {
                mEmailLogin.logout();
                updateUI(false);
            }
        } else if (resId == R.id.btSignInGoogle) {
            signInGoogle();
        } else if (resId == R.id.btFriend) {
            Intent intent = new Intent(getApplicationContext(), ActivityFriendMain.class);
            startActivity(intent);
        }
    }

    public void signInGoogle() {
        Log.d(TAG, "signInGoogle!");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();//Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mCurrentUser == null)
            updateUI(false);
        else
            updateUI(true);
    }

    public void updateUI(boolean isLogin) {
        if (isLogin) {
            Toast.makeText(getApplicationContext(), "로그인 되어 있습니다.!", Toast.LENGTH_SHORT).show();
//            FirebaseUser currentUser = mEmailLogin.getCurrentUser();

//            showProgress();
            saveUserData(mCurrentUser);
            String email = "Email : " + mCurrentUser.getEmail() + "\n";
            String displayName = "DisplayName : " + mCurrentUser.getDisplayName() + "\n";
            String uid = "Uid : " + mCurrentUser.getUid() + "\n";
            String phoneNumber = "PhoneNumber : " + mCurrentUser.getPhoneNumber() + "\n";
            String providerId = "ProviderId : " + mCurrentUser.getProviderId() + "\n";
            String photoUrl = "PhotoUrl : " + mCurrentUser.getPhotoUrl() + "\n";
            String token = "Token : " + Define.TOKEN + "\n";

            Log.d(TAG, email + displayName + uid + phoneNumber + providerId + photoUrl + token);

            TextView tvUserInfo = findViewById(R.id.tvUserInfo);
            tvUserInfo.setText(email + displayName + uid + phoneNumber + providerId + photoUrl + token);

            LinearLayout llLoginView = findViewById(R.id.llLoginView);
            llLoginView.setVisibility(View.GONE);

            LinearLayout llUserInfoView = findViewById(R.id.llUserInfoView);
            llUserInfoView.setVisibility(View.VISIBLE);
        } else {
            LinearLayout llLoginView = findViewById(R.id.llLoginView);
            llLoginView.setVisibility(View.VISIBLE);

            TextView tvUserInfo = findViewById(R.id.tvUserInfo);
            tvUserInfo.setText("");

            LinearLayout llUserInfoView = findViewById(R.id.llUserInfoView);
            llUserInfoView.setVisibility(View.GONE);
        }
    }

    public void saveUserData(final FirebaseUser currentUser) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        User user = document.toObject(User.class);
                        if (user == null || user.getUid() == null || user.getUid().equalsIgnoreCase("")) {
                            user = new User(currentUser.getEmail(), currentUser.getDisplayName(), currentUser.getPhoneNumber(), currentUser.getProviderId(), currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "", Define.TOKEN, currentUser.getUid());
                        } else {
                            user.setUid(currentUser.getUid());
                            user.setToken(Define.TOKEN);
                        }

                        user.setPhoneNumber(Utility.getPhoneNumber(getApplicationContext()));

                        db.collection(Define.FB_DB_COLLECTION_USERS).document(currentUser.getUid()).set(user);

                        Define.LOGIN_USER = user;
                    } else {
                        User user = new User(currentUser.getEmail(), currentUser.getDisplayName(), currentUser.getPhoneNumber(), currentUser.getProviderId(), currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "", Define.TOKEN, currentUser.getUid());

                        user.setPhoneNumber(Utility.getPhoneNumber(getApplicationContext()));

                        db.collection(Define.FB_DB_COLLECTION_USERS).document(currentUser.getUid()).set(user);

                        Define.LOGIN_USER = user;
                    }

                    closeProgress();
                    startFriendList();
                }
            }
        });
    }

    public void setView() {
        mEtId = findViewById(R.id.etId);
        mEtPwd = findViewById(R.id.etPwd);
        SignInButton mGoogleSignButton = findViewById(R.id.btSignInGoogle);
        mGoogleSignButton.setOnClickListener(this);
    }

    public void setEmailLogin(boolean isCreateAccount) {
        if (mEmailLogin == null)
            mEmailLogin = new FirebaseEmailLogin(getApplicationContext());

        if (!mEmailLogin.isLogin()) {
            boolean isValidEmail = false;
            boolean isValidPassword = false;

            if (Utility.isValidEmail(mEtId.getText().toString().trim())) {
                isValidEmail = true;
            } else {
                Toast.makeText(getApplicationContext(), "이메일을 확인해 주세요.", Toast.LENGTH_SHORT).show();
            }

            if (Utility.isValidPassword(mEtPwd.getText().toString().trim())) {
                isValidPassword = true;
            } else {
                Toast.makeText(getApplicationContext(), "패스워드는 6글자이상 영문 숫자만 가능합니다.", Toast.LENGTH_SHORT).show();
            }

            if (isValidEmail && isValidPassword) {
                if (!isCreateAccount) {
                    mEmailLogin.signLoginEmail(this, mEtId.getText().toString().trim(), mEtPwd.getText().toString().trim(), new LoginListener() {
                        @Override
                        public void loginResult(boolean isResult, FirebaseUser currentUser, String exception) {
                            mCurrentUser = currentUser;
                            showProgress();
                            updateUI(isResult);
                            if (isResult) {
                                Toast.makeText(getApplicationContext(), currentUser.getEmail() + " 로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    mEmailLogin.createLoginEmail(this, mEtId.getText().toString().trim(), mEtPwd.getText().toString().trim(), new LoginListener() {
                        @Override
                        public void loginResult(boolean isResult, FirebaseUser currentUser, String exception) {
                            mCurrentUser = currentUser;
                            showProgress();
                            updateUI(isResult);
                            if (isResult) {
                                Toast.makeText(getApplicationContext(), currentUser.getEmail() + " 계정 생성에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "계정생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }

    }

    public void startFriendList() {
        Intent intent = new Intent(getApplicationContext(), ActivityFriendMain.class);
        startActivity(intent);
        finish();
    }

    ProgressDialog mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);

            mProgressDialog.setMessage("로그인 중입니다.");

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
