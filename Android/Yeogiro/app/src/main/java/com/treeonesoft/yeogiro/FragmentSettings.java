package com.treeonesoft.yeogiro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FragmentSettings extends Fragment {
    private static final String TAG = "SESIN_FS";

    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_PICTURE_CROP = 2;

    Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_settings, parentViewGroup, false);
        setProfile(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach context");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach activity");
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    ImageView mIvProfile;
    EditText mEtName;

    TextView mTvProfileChange;
    TextView mTvNameChange;

    public void setProfile(View view) {
        mIvProfile = view.findViewById(R.id.ivProfileImg);
        mEtName = view.findViewById(R.id.etName);

        mTvProfileChange = view.findViewById(R.id.tvProfileChange);

        mTvProfileChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
//                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType(TYPE_IMAGE);
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,
                        "Profile Image Select"), SELECT_PICTURE);
            }
        });
        mTvNameChange = view.findViewById(R.id.tvNameChange);
        mTvNameChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEtName.getText())) {
                    Define.LOGIN_USER.setDisplayName(mEtName.getText().toString());
                    showProgress("저장중입니다.");
                    saveUserData(Define.LOGIN_USER);
                }
            }
        });

        if (Define.LOGIN_USER.getPhotoUrl() != null && Define.LOGIN_USER.getPhotoUrl().startsWith("http")) {
            Log.d("SESIN_AD", "photoUrl = " + Define.LOGIN_USER.getPhotoUrl());
            Glide.with(mActivity).load(Define.LOGIN_USER.getPhotoUrl()).into(mIvProfile);
        } else {
            Log.d("SESIN_AD", "photoUrl null");
            mIvProfile.setImageResource(R.drawable.noprofile);
        }

        if (Define.LOGIN_USER.getDisplayName() != null) {
            mEtName.setText(Define.LOGIN_USER.getDisplayName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageUri = data.getData();
                Log.d("SESIN", "selectedImageUri = " + selectedImageUri.toString());
                Log.d("SESIN", RealPathUtil.getRealPath(mActivity, selectedImageUri) == null ? "path is null!" : "not null " + RealPathUtil.getRealPath(mActivity, selectedImageUri));

                Uri uri = Uri.parse(RealPathUtil.getRealPath(mActivity, selectedImageUri));
                Log.d("SESIN", "uri = " + uri.toString());
                cropImage(selectedImageUri);
//                saveProfileImage(Define.LOGIN_USER.getUid(), RealPathUtil.getRealPath(mActivity, selectedImageUri));
            }
        } else if (requestCode == SELECT_PICTURE_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("SESIN", RealPathUtil.getRealPath(mActivity, mTempImageUri) == null ? "path is null!" : "not null " + RealPathUtil.getRealPath(mActivity, mTempImageUri));
                showProgress("변경중입니다.");
                saveProfileImage(Define.LOGIN_USER.getUid(), RealPathUtil.getRealPath(mActivity, mTempImageUri));
            }
        }
    }

    private static final String TYPE_IMAGE = "image/*";
    private static final int PROFILE_IMAGE_ASPECT_X = 1;
    private static final int PROFILE_IMAGE_ASPECT_Y = 1;
    private static final int PROFILE_IMAGE_OUTPUT_X = 500;
    private static final int PROFILE_IMAGE_OUTPUT_Y = 500;

    private static final String TEMP_FILE_NAME = ".profileImageTemp.jpg";

    private Uri mTempImageUri;

    private File getTempFile() {

        Log.d("SESIN_CROP", "path=" + Environment.getExternalStorageDirectory().getAbsolutePath());
        File file = new File(Environment.getExternalStorageDirectory(), TEMP_FILE_NAME);
        try {
            file.createNewFile();
        } catch (Exception e) {
            Log.e("kingpig", "fileCreation fail");
        }
        return file;
    }


    FirebaseStorage mFirebaseStorage;

    public void saveProfileImage(String uid, String filePath) {
        if (mFirebaseStorage == null)
            mFirebaseStorage = FirebaseStorage.getInstance();

        StorageReference storageRef = mFirebaseStorage.getReference();

        Uri file = Uri.fromFile(new File(filePath));

        StorageReference profileRef = storageRef.child("profile/" + uid + "/" + file.getLastPathSegment());

        UploadTask uploadTask = profileRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("SESIN", "save fail");
                Toast.makeText(mActivity, "이미지 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                closeProgress();
            }
        });

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("SESIN", "uri = " + downloadUrl.toString());
                Define.LOGIN_USER.setPhotoUrl(downloadUrl.toString());
                saveUserData(Define.LOGIN_USER);
            }
        });
    }

    public void cropImage(Uri imgUri) {

        mActivity.grantUriPermission("com.android.camera", imgUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imgUri, TYPE_IMAGE);

        List<ResolveInfo> list = mActivity.getPackageManager().queryIntentActivities(intent, 0);

        boolean isException = false;

        for(int i = 0 ; i < list.size() ; i++ )
        {
            try {
                mActivity.grantUriPermission(list.get(i).activityInfo.packageName, imgUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }catch(Exception e)
            {
                e.printStackTrace();
                isException = true;
                Log.d("SESIN_CROP", i + " " + e.getMessage());
            }

            if( !isException ) {
                Log.d("SESIN_CROP", i + " pkgName=" + list.get(i).activityInfo.packageName);
                break;
            }
        }

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Toast.makeText(mActivity, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", PROFILE_IMAGE_ASPECT_X);
        intent.putExtra("aspectY", PROFILE_IMAGE_ASPECT_Y);
        intent.putExtra("outputX", PROFILE_IMAGE_OUTPUT_X);
        intent.putExtra("outputY", PROFILE_IMAGE_OUTPUT_Y);

        intent.putExtra("scale", true);

        mTempImageUri = Uri.fromFile(getTempFile());

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

        Intent i = new Intent(intent);

        ResolveInfo res = null;

        isException = false;
        for( int j = 0 ; j < list.size() ; j++ )
        {
            res = list.get(j);

            try {
                mActivity.grantUriPermission(res.activityInfo.packageName, mTempImageUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }catch(Exception e)
            {
                e.printStackTrace();
                res = null;
                isException = true;
                Log.d("SESIN_CROP", j + " " + e.getMessage());
            }

            if( !isException ) {
                Log.d("SESIN_CROP", j + " res = " + res.activityInfo.packageName + " " + res.activityInfo.name);
                break;
            }
        }

        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if( res != null )
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

        startActivityForResult(i, SELECT_PICTURE_CROP);
    }

    public void saveUserData(User currentUser) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Define.FB_DB_COLLECTION_USERS).document(currentUser.getUid()).set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                closeProgress();
                if (Define.LOGIN_USER.getPhotoUrl() != null && Define.LOGIN_USER.getPhotoUrl().startsWith("http")) {
                    Log.d("SESIN_AD", "photoUrl = " + Define.LOGIN_USER.getPhotoUrl());
                    Glide.with(mActivity).load(Define.LOGIN_USER.getPhotoUrl()).into(mIvProfile);
                    Toast.makeText(mActivity, "이미지 변경에 성공했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("SESIN_AD", "photoUrl null");
//                    mIvProfile.setImageResource(R.drawable.noprofile);
                    Toast.makeText(mActivity, "이미지 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }

                if (Define.LOGIN_USER.getDisplayName() != null) {
                    mEtName.setText(Define.LOGIN_USER.getDisplayName());
                    Toast.makeText(mActivity, "이름 변경에 성공했습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgress();
            }
        });
    }

    ProgressDialog mProgressDialog;

    public void showProgress(String body) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mActivity);

            mProgressDialog.setMessage(body);

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
