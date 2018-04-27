package com.treeonesoft.yeogiro;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;
import com.treeonesoft.yeogiro.was.RequestURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, ItemClickListener, View.OnClickListener {
    private static final String TAG = "SESIN_AM";

    private static final int PIN_WIDTH = 150;
    private static final int PIN_HEIGHT = 150;

    GoogleMap mGoogleMap;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    Marker mMyMarker;

    Marker mTargetMarker;

    Map<String, Marker> mFriendsMarker = new HashMap<String, Marker>();

    Socket mSocket;

    String mRoomId = null;

    ArrayList<User> mFriendList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    InputMethodManager mImm;

    String mFriendUid;

    LatLng mCurrentLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.frMapContainer);
        mapFragment.getMapAsync(this);

        registerUIReceiver();

        mRoomId = getIntent().getStringExtra("roomId");

        Log.d(TAG, "roomId=" + mRoomId);

        debug(mRoomId);

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void debug(String string) {
        TextView debug = findViewById(R.id.tvDebug);
        debug.setText(debug.getText() + "\n" + string);
    }

    @SuppressLint("RestrictedApi")
    public void createLocationRequest() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnMapLongClickListener(this);

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, marker.getPosition().toString() + " " + marker.getTitle());
                if (marker.getSnippet().equals("목적지")) {
                    return false;
                }

                return false;
                /*
                if (marker.getSnippet().equals("나")) {
                    Set<Map.Entry<String, Marker>> set = mFriendsMarker.entrySet();
                    Iterator<Map.Entry<String, Marker>> iterator = set.iterator();
                    Log.d(TAG, iterator.hasNext() + " ");
                    while (iterator.hasNext()) {
                        Map.Entry<String, Marker> m = (Map.Entry<String, Marker>) iterator.next();
                        Log.d(TAG, m.getKey() + " " + m.getValue().getClass().equals(Marker.class));

                        Marker friendMarker = mFriendsMarker.get(m.getKey());
                        if (SphericalUtil.computeDistanceBetween(marker.getPosition(), friendMarker.getPosition()) <= 100.0f) {
                            Toast.makeText(getApplicationContext(), "100m보다 작으면 거리를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        showTarget(FCMProtocol.DataType.TARGET_MF, Define.LOGIN_USER.getUid(), marker.getPosition());

                        mTargetMarker = marker;
                    }
                } else {
                    if (SphericalUtil.computeDistanceBetween(marker.getPosition(), mMyMarker.getPosition()) <= 100.0f) {
                        Toast.makeText(getApplicationContext(), "100m보다 작으면 거리를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        showTarget(FCMProtocol.DataType.TARGET_MF, getMarkerUid(marker.getTitle()), marker.getPosition());
                        mTargetMarker = marker;
                    }
                }

                return false;
                */
            }
        });

//        startLocation();

        setFriendView();
    }

    public String getMarkerUid(String displayName) {
        for (int i = 0; i < mFriendList.size(); i++) {
            if (displayName.equalsIgnoreCase(mFriendList.get(i).getDisplayName()))
                return mFriendList.get(i).getUid();
        }

        return null;
    }

    @SuppressLint("MissingPermission")
    public void startLocation() {

        if (Define.DEBUG) {
            mCurrentLatLng = new LatLng(Double.parseDouble(Define.LOGIN_USER.getLastLat()), Double.parseDouble(Define.LOGIN_USER.getLastLng()));

            if (mMyMarker == null) {
                MarkerOptions MyMarker = new MarkerOptions();
                MyMarker.position(mCurrentLatLng);
                MyMarker.title(Define.LOGIN_USER.getDisplayName());
                MyMarker.snippet("나");
                MyMarker.draggable(true);

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.people_pin);
                MyMarker.icon(BitmapDescriptorFactory.fromBitmap(Utility.tintImage(Bitmap.createScaledBitmap(bitmap, PIN_WIDTH, PIN_HEIGHT, true), Color.RED)));

                mMyMarker = mGoogleMap.addMarker(MyMarker);

                mCurrentLatLng = mMyMarker.getPosition();

                CameraPosition cameraPosition = new CameraPosition.Builder().target(MyMarker.getPosition()).zoom(15).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                connectSocket();
            } else {
                mMyMarker.setPosition(mCurrentLatLng);
                mCurrentLatLng = mMyMarker.getPosition();
                sendMessage("LOCATION", Define.LOGIN_USER.getUid(), mRoomId, mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude);
            }

            return;
        }

        createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;

                int i = 1;
                for (Location location : locationResult.getLocations()) {
                    Log.d("SESIN", i + " provider:" + location.getProvider() + " " + location.getLatitude() + " " + location.getLongitude());
                    i++;
                    if (mMyMarker == null) {
                        MarkerOptions MyMarker = new MarkerOptions();
                        MyMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                        MyMarker.title(Define.LOGIN_USER.getDisplayName());
                        MyMarker.snippet("나");
                        MyMarker.draggable(true);

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.people_pin);
                        MyMarker.icon(BitmapDescriptorFactory.fromBitmap(Utility.tintImage(Bitmap.createScaledBitmap(bitmap, PIN_WIDTH, PIN_HEIGHT, true), Color.RED)));

                        mMyMarker = mGoogleMap.addMarker(MyMarker);

                        mCurrentLatLng = mMyMarker.getPosition();

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(MyMarker.getPosition()).zoom(15).build();
                        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        connectSocket();
                    } else {
                        mMyMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        mCurrentLatLng = mMyMarker.getPosition();
                        sendMessage("LOCATION", Define.LOGIN_USER.getUid(), mRoomId, location.getLatitude() + "," + location.getLongitude());
                    }

                    calDistance();
                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    public void stopLocation() {
        if (mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationCallback = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
        unregisterUIReceiver();
        disconnectSocket();

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if (SphericalUtil.computeDistanceBetween(mMyMarker.getPosition(), latLng) <= 100.0f) {
            Toast.makeText(getApplicationContext(), "100m보다 작으면 거리를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mTargetMarker != null) {
            mTargetMarker.remove();
            mTargetMarker = null;
        }

        if (mTargetMarker == null) {
            MarkerOptions MyMarker = new MarkerOptions();
            MyMarker.position(latLng);
            MyMarker.snippet("목적지");
            MyMarker.draggable(true);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.place_pin);
            MyMarker.icon(BitmapDescriptorFactory.fromBitmap(Utility.tintImage(Bitmap.createScaledBitmap(bitmap, PIN_WIDTH, PIN_HEIGHT, true), Color.MAGENTA)));

//            MyMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

            mTargetMarker = mGoogleMap.addMarker(MyMarker);

            moveCamera(latLng, mGoogleMap.getCameraPosition().zoom);
        }

        showTarget(FCMProtocol.DataType.TARGET_PLACE, null, latLng);
    }

    public void moveCamera(LatLng latLng, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(zoomLevel).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void setFriendView() {
        showProgress("로딩중입니다.");
        mRecyclerView = (RecyclerView) findViewById(R.id.rvFriend);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getFriendListData();
    }

    public void getFriendListData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (mFriendList == null)
                        mFriendList = new ArrayList<User>();

                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d("SESIN", document.getId() + "=>" + document.getData());
                        User user = document.toObject(User.class);
                        if (!document.getId().equalsIgnoreCase(Define.LOGIN_USER.getUid())) {
                            String myUid = Define.LOGIN_USER.getUid();

                            ArrayList<String> myFriends = Define.LOGIN_USER.getFriends();
                            ArrayList<String> friendFriends = user.getFriends();

                            boolean myFriend = false;
                            boolean youFriend = false;

                            if (myFriends != null && friendFriends != null) {
                                for (int i = 0; i < myFriends.size(); i++) {
                                    if (myFriends.get(i).equalsIgnoreCase(user.getUid())) {
                                        myFriend = true;
                                        break;
                                    }
                                }

                                for (int i = 0; i < friendFriends.size(); i++) {
                                    if (friendFriends.get(i).equalsIgnoreCase(myUid)) {
                                        youFriend = true;
                                        break;
                                    }
                                }

                                if (myFriend && youFriend) {
                                    mFriendList.add(user);
                                }
                            }
                        }
                    }

                    mAdapter = new HLVAdapter(getApplicationContext(), mFriendList, ActivityMap.this);
                    mRecyclerView.setAdapter(mAdapter);
                    startLocation();
                }
            }
        });
    }

    public void showTarget(final FCMProtocol.DataType dataType, final String targetUid, final LatLng latLng) {
        final LinearLayout dialog = findViewById(R.id.llDialog);

        if (dialog.getVisibility() == View.GONE) {
            dialog.setVisibility(View.VISIBLE);
            TextView tvBody = findViewById(R.id.tvDialogBody);
            tvBody.setText("이 위치로 목적지를 설정하시겠습니까?");

            TextView tvOk = findViewById(R.id.tvDialogOk);
            tvOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgress("목적지 설정 중 입니다..");
                    dialog.setVisibility(View.GONE);
                    if (dataType.equals(FCMProtocol.DataType.TARGET_MF)) {
                        sendMessage(dataType.toString(), Define.LOGIN_USER.getUid(), mRoomId, targetUid);
                    } else {
                        sendMessage(dataType.toString(), Define.LOGIN_USER.getUid(), mRoomId, latLng.latitude + "," + latLng.longitude);
                    }

                }
            });

            TextView tvCancel = findViewById(R.id.tvDialogCancel);
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.setVisibility(View.GONE);
                    if (mTargetMarker != null) {
                        mTargetMarker.remove();
                        mTargetMarker = null;

                    }
                }
            });
        }
    }

    Marker mFriendMarker = null;

    public void setFriendMarker(final LatLng latLng, final User friendUser) {
        Marker friendMarker = mFriendsMarker.get(friendUser.getUid());

        if (friendMarker == null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MarkerOptions FriendMarker = new MarkerOptions();
                    FriendMarker.position(latLng);
                    FriendMarker.title(friendUser.getDisplayName());
                    FriendMarker.snippet("친구");
//                    FriendMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.people_pin);
                    FriendMarker.icon(BitmapDescriptorFactory.fromBitmap(Utility.tintImage(Bitmap.createScaledBitmap(bitmap, PIN_WIDTH, PIN_HEIGHT, true), Color.BLUE)));
                    FriendMarker.draggable(true);

                    Marker friendMarker = mGoogleMap.addMarker(FriendMarker);
                    mFriendsMarker.put(friendUser.getUid(), friendMarker);
                    mFriendMarker = friendMarker;
//                    calDistance(mMyMarker.getPosition(), friendMarker.getPosition());
                }
            });
        } else {
            Double lat = latLng.latitude;
            Double lng = latLng.longitude;

            moveMarker(friendMarker, lat, lng);

        }

        calDistance();
    }

    public void moveMarker(final Marker marker, final Double lat, final Double lng) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                marker.setPosition(new LatLng(lat, lng));
            }
        });
    }

    synchronized public void calDistance(LatLng me, LatLng target) {
        final double dis = SphericalUtil.computeDistanceBetween(me, target);

        Log.d(TAG, "cal = " + dis);
        if (dis < 50.0f) {
            Toast.makeText(getApplicationContext(), "서로의 위치가 50m입니다. 고개를 들어 확인해 주세요~", Toast.LENGTH_SHORT).show();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mResult.setText("약 " + dis + " m");
            }
        });
    }

    public User getFriendUser(String uid) {
        User user;

        for (int i = 0; i < mFriendList.size(); i++) {
            user = mFriendList.get(i);

            if (user.getUid().equalsIgnoreCase(uid)) {
                return user;
            }
        }

        return null;
    }

    public void processTargetPlace(final String name, final String msg) {
        if (mTargetMarker == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] result = msg.split(",");
                    Double lat = Double.parseDouble(result[0]);
                    Double lng = Double.parseDouble(result[1]);

                    LatLng latLng = new LatLng(lat, lng);

                    MarkerOptions MyMarker = new MarkerOptions();
                    MyMarker.position(latLng);
                    MyMarker.snippet("목적지");
                    MyMarker.draggable(true);
//                    MyMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.place_pin);
                    MyMarker.icon(BitmapDescriptorFactory.fromBitmap(Utility.tintImage(Bitmap.createScaledBitmap(bitmap, PIN_WIDTH, PIN_HEIGHT, true), Color.MAGENTA)));

                    mTargetMarker = mGoogleMap.addMarker(MyMarker);

                    moveCamera(latLng, mGoogleMap.getCameraPosition().zoom);

                    showTarger();
                }
            });
        }
    }

    public void showTarger() {
        final LinearLayout dialog = findViewById(R.id.llDialog);

        if (dialog.getVisibility() == View.GONE) {
            dialog.setVisibility(View.VISIBLE);
            TextView tvBody = findViewById(R.id.tvDialogBody);
            tvBody.setText("이 위치로 목적지 요청이 왔습니다. " + "이 위치로 설정하시겠습니까?");

            TextView tvOk = findViewById(R.id.tvDialogOk);
            tvOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.setVisibility(View.GONE);
                    sendMessage(FCMProtocol.DataType.TARGET_PLACE_A.toString(), Define.LOGIN_USER.getUid(), mRoomId, "Y");
                    writeLine(mMyMarker.getPosition(), mTargetMarker.getPosition(), mMyMarker.getSnippet(), mTargetMarker.getSnippet());
                }
            });

            TextView tvCancel = findViewById(R.id.tvDialogCancel);
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.setVisibility(View.GONE);
                    sendMessage(FCMProtocol.DataType.TARGET_PLACE_A.toString(), Define.LOGIN_USER.getUid(), mRoomId, "N");
                    if (mTargetMarker != null) {
                        mTargetMarker.remove();
                        mTargetMarker = null;
                    }
                }
            });
        }
    }

    public void processTargetMF(final String name, final String msg) {
        if (Define.LOGIN_USER.getUid().equalsIgnoreCase(msg)) // 내위치?
        {
            showTarget("내", name);
        } else // 친구위치
        {
            showTarget(getFriendName(msg), name);
        }
    }

    public String getFriendName(String uid) {
        for (int i = 0; i < mFriendList.size(); i++) {
            if (uid.equalsIgnoreCase(mFriendList.get(i).getUid()))
                return mFriendList.get(i).getDisplayName();
        }
        return null;
    }

    public void showTarget(final String name, final String uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LinearLayout dialog = findViewById(R.id.llDialog);

                if (dialog.getVisibility() == View.GONE) {
                    dialog.setVisibility(View.VISIBLE);
                    TextView tvBody = findViewById(R.id.tvDialogBody);
                    tvBody.setText(name + " 위치로 목적지 요청이 왔습니다. " + "이 위치로 설정하시겠습니까?");

                    TextView tvOk = findViewById(R.id.tvDialogOk);
                    tvOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.setVisibility(View.GONE);
                            sendMessage(FCMProtocol.DataType.TARGET_MF_A.toString(), Define.LOGIN_USER.getUid(), mRoomId, "Y");
                            mTargetMarker = mFriendsMarker.get(uid);
                            writeLine(mMyMarker.getPosition(), mTargetMarker.getPosition(), mMyMarker.getSnippet(), mMyMarker.getSnippet());
                        }
                    });

                    TextView tvCancel = findViewById(R.id.tvDialogCancel);
                    tvCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.setVisibility(View.GONE);
                            sendMessage(FCMProtocol.DataType.TARGET_MF_A.toString(), Define.LOGIN_USER.getUid(), mRoomId, "N");
                            if (mTargetMarker != null) {
//                                mTargetMarker.remove();
                                mTargetMarker = null;
                            }
                        }
                    });
                }
            }
        });

    }

    public void processTargetPlaceAnswer(String name, String msg) {
        closeProgress();
        if (!name.equals(Define.LOGIN_USER.getUid())) {
            if (msg.equalsIgnoreCase("N")) {
                Toast.makeText(getApplicationContext(), "거절 했어요!", Toast.LENGTH_SHORT).show();
                if (mTargetMarker != null) {
                    mTargetMarker.remove();
                    mTargetMarker = null;
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeLine(mMyMarker.getPosition(), mTargetMarker.getPosition(), mMyMarker.getSnippet(), mTargetMarker.getSnippet());
                    }
                });
            }
        }
    }

    public void processTargetMFAnswer(String name, String msg) {
        closeProgress();
        if (msg.equalsIgnoreCase("N")) {
            Toast.makeText(getApplicationContext(), "거절 했어요!", Toast.LENGTH_SHORT).show();
            if (mTargetMarker != null) {
                mTargetMarker = null;
            }
        } else {
            if (mTargetMarker.getSnippet().equals("나")) {
                mTargetMarker = mFriendsMarker.get(name);
                writeLine(mMyMarker.getPosition(), mTargetMarker.getPosition(), mMyMarker.getSnippet(), mTargetMarker.getSnippet());
            } else {
                writeLine(mMyMarker.getPosition(), mTargetMarker.getPosition(), mMyMarker.getSnippet(), mTargetMarker.getSnippet());
            }
        }
    }

    LinearLayout mLlContainer;

    public void connectSocket() {
        if (mRoomId == null) return;

        try {
            mSocket = IO.socket("http://14.63.197.225:3000/chat");

            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("SESIN_SOCKET", "connect!");
                    sendMessage("CONNECT", Define.LOGIN_USER.getUid(), mRoomId, null);

                    if (mCurrentLatLng != null)
                        sendMessage(FCMProtocol.DataType.LOCATION.toString(), Define.LOGIN_USER.getUid(), mRoomId, mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude);

                    sendMessage(FCMProtocol.DataType.LOCATION_WANT.toString(), Define.LOGIN_USER.getUid(), mRoomId, "Test");
                }

            }).on("message", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("SESIN_SOCKET", "read! " + String.valueOf(args[0]));
                    try {
                        JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                        String type = jsonObject.getString("type");
                        final String name = jsonObject.getString("name");
                        String room = jsonObject.getString("room");
                        final String msg = jsonObject.getString("msg");

                        Log.d("SESIN_SOCKET", "name:" + name + " " + type);

                        if (type.equalsIgnoreCase("LOCATION")) {
                            if (name.equalsIgnoreCase(Define.LOGIN_USER.getUid())) return;

//                            mFriendUid = name;

                            String[] result = msg.trim().split(",");

                            Double lat = Double.parseDouble(result[0]);
                            Double lng = Double.parseDouble(result[1]);
                            setFriendMarker(new LatLng(lat, lng), getFriendUser(name));
                            closeProgress();
                            Log.d("SESIN_SOCKET", "location!");
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.LOCATION_WANT.toString())) {
                            if (mCurrentLatLng != null)
                                sendMessage(FCMProtocol.DataType.LOCATION.toString(), Define.LOGIN_USER.getUid(), mRoomId, mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude);
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.DISCONNECT.toString())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "친구가 여기로를 종료했습니다. 지도화면을 종료합니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.TARGET_PLACE.toString())) {
                            processTargetPlace(name, msg);
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.TARGET_MF.toString())) {
                            processTargetMF(name, msg);
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.TARGET_PLACE_A.toString())) {
                            processTargetPlaceAnswer(name, msg);
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.TARGET_MF_A.toString())) {
                            processTargetMFAnswer(name, msg);
                        } else if (type.equalsIgnoreCase(FCMProtocol.DataType.CHAT.toString())) {
                            if (name.equalsIgnoreCase(Define.LOGIN_USER.getUid())) {
                                if (mLlContainer == null)
                                    mLlContainer = findViewById(R.id.llChatContainer);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatLinearLayout chat = new ChatLinearLayout(getApplicationContext());
                                        chat.setMyChat(msg);
                                        mLlContainer.addView(chat);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((ScrollView) findViewById(R.id.scChat)).fullScroll(ScrollView.FOCUS_DOWN);
                                            }
                                        }, 100);

                                        if (findViewById(R.id.llChat).getVisibility() == View.GONE) {
                                            TextView tvChat = findViewById(R.id.tvChat);
                                            if (tvChat.getText().toString().contains("^")) {
                                                String result = tvChat.getText().toString();
                                                result = result.replace("^", "1");
                                                tvChat.setText(result);
                                            } else {
                                                String[] result = tvChat.getText().toString().split("\n");
                                                int cnt = Integer.parseInt(result[0]);
                                                String text = (cnt + 1) + "\nChat";
                                                tvChat.setText(text);
                                            }

                                        }
                                    }
                                });

                            } else {
                                if (mLlContainer == null)
                                    mLlContainer = findViewById(R.id.llChatContainer);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatLinearLayout chat = new ChatLinearLayout(getApplicationContext());
                                        chat.setFriendChat(getFriendUser(name).getDisplayName(), msg, getFriendUser(name).getPhotoUrl());
                                        mLlContainer.addView(chat);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((ScrollView) findViewById(R.id.scChat)).fullScroll(ScrollView.FOCUS_DOWN);
                                            }
                                        }, 100);

                                        if (findViewById(R.id.llChat).getVisibility() == View.GONE) {
                                            Log.d(TAG, "llChat gone!");

                                            TextView tvChat = findViewById(R.id.tvChat);
                                            if (tvChat.getText().toString().contains("^")) {
                                                Log.d(TAG, "contains");
                                                String result = tvChat.getText().toString();
                                                result = result.replace("^", "1");
                                                tvChat.setText(result);
                                            } else {
                                                Log.d(TAG, "number");
                                                String[] result = tvChat.getText().toString().split("\n");
                                                int cnt = Integer.parseInt(result[0]);
                                                String text = (cnt + 1) + "\nChat";
                                                tvChat.setText(text);

                                            }

                                        }
                                    }
                                });

                            }
                        } else {
                            Log.d("SESIN_SOCKET", "location! NONONONONONO");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("SESIN_SOCKET", "socket error : " + e.getMessage());
                    }
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("SESIN_SOCKET", "disconnect");
                }

            });
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("SESIN_SOCKET", "msg:" + e.getMessage());
        }
    }

    public void disconnectSocket() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
    }

    public void sendMessage(String type, String name, String room, String msg) {
        if (mSocket != null) {
            Map<String, String> data = new HashMap<String, String>();

            data.put("type", type);
            data.put("name", name);
            data.put("room", room);
            if (msg == null)
                data.put("msg", "");
            else
                data.put("msg", msg);

            mSocket.emit("message", Utility.getJsonStringFromMap(data));
        }
    }

    @Override
    public void onClick(View view, int position, boolean isLongClick) {
        if (findViewById(R.id.llDialog).getVisibility() == View.VISIBLE)
            return;
        mFriendUid = mFriendList.get(position).getUid();
        TextView tvCall = findViewById(R.id.tvCall);

        if (tvCall.getVisibility() == View.GONE) {
            tvCall.setVisibility(View.VISIBLE);
            tvCall.setText(mFriendList.get(position).getDisplayName() + "\n" + "전화걸기");
        } else {
            tvCall.setVisibility(View.GONE);
        }
    }

    public void showLocationDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치정보요청");
        builder.setMessage(user.getDisplayName() + "에게 위치정보를 요청하시겠습니까?");
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utility.sendMessage(FCMProtocol.DataType.LOCATION, FCMProtocol.RequestType.QUESTION, mMyMarker.getPosition().latitude + "," + mMyMarker.getPosition().longitude, Define.LOGIN_USER.getToken(),
                        Define.LOGIN_USER.getDisplayName(), Define.LOGIN_USER.getUid(), user.getToken());
                dialog.cancel();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "위치정보를 요청했습니다!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    ProgressDialog mProgressDialog;

    public void showProgress(String body) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);

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

    @Override
    public void onClick(View v) {
        int resId = v.getId();

        if (findViewById(R.id.llDialog).getVisibility() == View.VISIBLE)
            return;

        switch (resId) {
            case R.id.tvChat: {
                mRecyclerView.setVisibility(View.GONE);
                TextView tvChat = findViewById(R.id.tvChat);
                findViewById(R.id.llAction).setVisibility(View.GONE);
                tvChat.setText("^\nChat");

                findViewById(R.id.llChat).setVisibility(View.VISIBLE);

                EditText etChat = findViewById(R.id.etChat);
                etChat.requestFocus();
                mImm.showSoftInput(etChat, InputMethodManager.SHOW_FORCED);

                break;
            }
            case R.id.tvCloseChat: {
                mRecyclerView.setVisibility(View.VISIBLE);
                TextView tvChat = findViewById(R.id.tvChat);

                findViewById(R.id.llAction).setVisibility(View.VISIBLE);

                findViewById(R.id.llChat).setVisibility(View.GONE);

                EditText etChat = findViewById(R.id.etChat);
                etChat.setText("");

                mImm.hideSoftInputFromWindow(etChat.getWindowToken(), 0);
                break;
            }
            case R.id.btChat: {
                EditText etChat = findViewById(R.id.etChat);
                if (etChat.getText().toString().trim().length() > 0) {
                    sendMessage("CHAT", Define.LOGIN_USER.getUid(), mRoomId, etChat.getText().toString());
                    etChat.setText("");
                }
                break;
            }
            case R.id.tvCall: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission("android.permission.CALL_PHONE") == PackageManager.PERMISSION_GRANTED) {
                        String phoneNumber = getFriendUser(mFriendUid).getPhoneNumber();
                        if (phoneNumber != null) {
                            Log.d(TAG, "phoneNumber " + phoneNumber);
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "저장된 친구의 번호가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "전화 걸기에 필요한 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String phoneNumber = mFriendList.get(0).getPhoneNumber();
                    if (phoneNumber != null) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "저장된 친구의 번호가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.llDialog).getVisibility() == View.VISIBLE) {
            findViewById(R.id.llDialog).setVisibility(View.GONE);
            if (mTargetMarker != null) {
                mTargetMarker.remove();
                mTargetMarker = null;
            }
        } else if (findViewById(R.id.llChat).getVisibility() == View.VISIBLE) {
            findViewById(R.id.llChat).setVisibility(View.GONE);

            mRecyclerView.setVisibility(View.VISIBLE);
//            findViewById(R.id.tvChat).setVisibility(View.VISIBLE);
            findViewById(R.id.llAction).setVisibility(View.VISIBLE);

            EditText etChat = findViewById(R.id.etChat);
            etChat.setText("");
        } else {
//            super.onBackPressed();
            showDestroyDialog();
        }
    }

    public void showDestroyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료");
        builder.setMessage("종료하시겠습니까? 친구와의 연결을 끊습니다.");
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage(FCMProtocol.DataType.DISCONNECT.toString(), Define.LOGIN_USER.getUid(), mRoomId, "disconnect");
                dialog.cancel();
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    BroadcastReceiver mUIReceiver;

    public void registerUIReceiver() {
        if (mUIReceiver == null) {
            mUIReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Define.ACTION_UI_CHANGE.equalsIgnoreCase(action)) {
                        String result = intent.getStringExtra("data");
                        if (result != null) {
                            Log.d("SESIN", "receive data : " + result);
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String dataType = jsonObject.getString(FCMProtocol.DATA_TYPE);
                                String reqType = jsonObject.getString(FCMProtocol.REQUEST_TYPE);
                                String message = jsonObject.getString(FCMProtocol.MESSAGE);
                                String senderToken = jsonObject.getString(FCMProtocol.SENDER_TOKEN);
                                String senderName = jsonObject.getString(FCMProtocol.SENDER_NAME);
                                String senderUid = jsonObject.getString(FCMProtocol.SENDER_UID);
                                String receiverToken = jsonObject.getString(FCMProtocol.RECEIVER_TOKEN);

                                if (dataType.equalsIgnoreCase(FCMProtocol.DataType.LOCATION.toString())) {
                                    if (reqType.equalsIgnoreCase(FCMProtocol.RequestType.QUESTION.toString())) {
                                        String[] loc = message.trim().split(",");
                                        Double lat = Double.parseDouble(loc[0]);
                                        Double lng = Double.parseDouble(loc[1]);

                                        showQuestionDialog(senderName, senderUid, senderToken, new LatLng(lat, lng));
                                    } else {
                                        String[] msgSplit = message.trim().split(",");
                                        if (msgSplit[0].equalsIgnoreCase("Y")) {
                                            Double lat = Double.parseDouble(msgSplit[1]);
                                            Double lng = Double.parseDouble(msgSplit[2]);

                                            mRoomId = Define.LOGIN_USER.getUid();
                                            connectSocket();
                                            setFriendMarker(new LatLng(lat, lng), getFriendUser(senderUid));
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("SESIN", "receive data is null!");
                        }
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Define.ACTION_UI_CHANGE);

            this.registerReceiver(mUIReceiver, intentFilter);
        }
    }

    public void unregisterUIReceiver() {
        if (mUIReceiver != null) {
            this.unregisterReceiver(mUIReceiver);
            mUIReceiver = null;
        }

    }

    public void showQuestionDialog(final String senderName, final String senderUid, final String senderToken, final LatLng senderLoc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치정보요청");
        builder.setMessage(senderName + "에게 위치정보를 요청받았습니다. 위치정보를 전송하시겠습니까?");
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utility.sendMessage(FCMProtocol.DataType.LOCATION, FCMProtocol.RequestType.ANSWER, "Y," + Define.LOGIN_USER.getLastLat() + "," + Define.LOGIN_USER.getLastLng(), Define.LOGIN_USER.getToken(),
                        Define.LOGIN_USER.getDisplayName(), Define.LOGIN_USER.getUid(), senderToken);
                dialog.cancel();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "위치정보를 전송했습니다!", Toast.LENGTH_SHORT).show();
                mRoomId = senderUid;
                connectSocket();

                setFriendMarker(senderLoc, getFriendUser(senderUid));
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
                Utility.sendMessage(FCMProtocol.DataType.LOCATION, FCMProtocol.RequestType.ANSWER, "N", Define.LOGIN_USER.getToken(),
                        Define.LOGIN_USER.getDisplayName(), Define.LOGIN_USER.getUid(), senderToken);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public static final String TMAP_URL = "https://api2.sktelecom.com/tmap/routes/pedestrian";

    public void writeLine(LatLng start, LatLng end, String startName, String endName) {
        Log.d(TAG + "TMAP", "writeLine!!");
        ContentValues values = new ContentValues();
        values.put("version", "1");
        values.put("startX", start.longitude);
        values.put("startY", start.latitude);
        values.put("endX", end.longitude);
        values.put("endY", end.latitude);
        values.put("startName", startName);
        values.put("endName", endName);
        values.put("appKey", getResources().getString(R.string.t_map_api_key));

        String url = TMAP_URL + "?" + "version=" + "1" + "&" +
                "startX=" + start.longitude + "&" +
                "startY=" + start.latitude + "&" +
                "endX=" + end.longitude + "&" +
                "endY=" + end.latitude + "&" +
                "startName=" + startName + "&" +
                "endName=" + endName + "&" +
                "appKey=" + getResources().getString(R.string.t_map_api_key);
        Log.d(TAG, "url = " + url);
        NetworkTask networkTask = null;

        if (startName.equals("나") && endName.equals("목적지"))
            networkTask = new NetworkTask(url, null, 0);
        else if (!startName.equals("나") && endName.equals("목적지"))
            networkTask = new NetworkTask(url, null, 1);
        else if (startName.equals("나") && !endName.equals("목적지"))
            networkTask = new NetworkTask(url, null, 2);

        networkTask.execute();
    }

    Polyline mMyPolyline;

    Polyline mFriendPolyline;

    public class NetworkTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;
        private int type;

        public NetworkTask(String url, ContentValues values, int type) {
            this.url = url;
            this.values = values;
            this.type = type;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            RequestURLConnection requestURLConnection = new RequestURLConnection();
            result = requestURLConnection.request(url, values);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG, "result = " + s);

            PolylineOptions plopt = null;

            if (s != null) {
                try {
                    JSONObject obj = new JSONObject(s);
                    JSONArray jsonArray = obj.getJSONArray("features");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        if (plopt == null) {
                            plopt = new PolylineOptions();
                            if (type == 0) {
                                plopt.color(Color.RED);
                                plopt.add(mMyMarker.getPosition());
                            } else if (type == 1) {
                                plopt.color(Color.BLUE);
                                plopt.add(mFriendMarker.getPosition());
                            } else if (type == 2) {
                                plopt.color(Color.MAGENTA);
                                plopt.add(mTargetMarker.getPosition());
                            }
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cObj = jsonArray.getJSONObject(i);
                            JSONObject geoMetry = cObj.getJSONObject("geometry");
                            if (geoMetry.getString("type").equals("LineString")) {

                                JSONArray coordinates = geoMetry.getJSONArray("coordinates");
                                for (int j = 0; j < coordinates.length(); j++) {
                                    String result = coordinates.get(j).toString();

                                    result = result.replace("[", "");
                                    result = result.replace("]", "");

                                    String[] latLng = result.trim().split(",");

                                    Double lat = Double.parseDouble(latLng[1]);
                                    Double lng = Double.parseDouble(latLng[0]);

                                    plopt.add(new LatLng(lat, lng));
                                    Log.d(TAG, "json array " + result);
                                }
                            }

                        }
                    }

                    if (type == 0) {
                        if (mMyPolyline == null) {
                            plopt.add(mTargetMarker.getPosition());
                            mMyPolyline = mGoogleMap.addPolyline(plopt);
                        } else {
                            mMyPolyline.remove();
                            plopt.add(mTargetMarker.getPosition());
                            mMyPolyline = mGoogleMap.addPolyline(plopt);
                        }

                        writeLine(mFriendMarker.getPosition(), mTargetMarker.getPosition(), mFriendMarker.getSnippet(), mTargetMarker.getSnippet());
                    } else if (type == 1) {
                        if (mFriendPolyline == null) {
                            plopt.add(mTargetMarker.getPosition());
                            mFriendPolyline = mGoogleMap.addPolyline(plopt);
                        } else {
                            mFriendPolyline.remove();
                            plopt.add(mTargetMarker.getPosition());
                            mFriendPolyline = mGoogleMap.addPolyline(plopt);
                        }
                        calDistance();
                    } else if (type == 2) {
                        if (mMyPolyline == null) {
                            plopt.add(mTargetMarker.getPosition());
                            mMyPolyline = mGoogleMap.addPolyline(plopt);
                        } else {
                            mMyPolyline.remove();
                            plopt.add(mTargetMarker.getPosition());
                            mMyPolyline = mGoogleMap.addPolyline(plopt);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "exception = " + e.getMessage());
                }
            }
        }
    }

    public void calDistance() {
        if (mMyMarker == null || mTargetMarker == null || mFriendMarker == null || mMyPolyline == null || mFriendPolyline == null)
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout llDis = findViewById(R.id.llDis);
                if (llDis.getVisibility() == View.GONE) {
                    llDis.setVisibility(View.VISIBLE);
                }

                int dis = (int) SphericalUtil.computeDistanceBetween(mMyMarker.getPosition(), mTargetMarker.getPosition());
                TextView mtDis = findViewById(R.id.tvMTDis);
                mtDis.setText("약 " + dis + "m");

                dis = (int) SphericalUtil.computeDistanceBetween(mMyMarker.getPosition(), mFriendMarker.getPosition());
                TextView mfDis = findViewById(R.id.tvMFDis);
                mfDis.setText("약 " + dis + "m");

                dis = (int) SphericalUtil.computeDistanceBetween(mFriendMarker.getPosition(), mTargetMarker.getPosition());
                TextView ftDis = findViewById(R.id.tvFTDis);
                ftDis.setText("약 " + dis + "m");
            }
        });

    }
}
