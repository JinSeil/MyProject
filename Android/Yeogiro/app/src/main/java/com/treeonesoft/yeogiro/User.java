package com.treeonesoft.yeogiro;

import java.util.ArrayList;

public class User
{
    private String email;
    private String displayName;
    private String phoneNumber;
    private String providerId;
    private String photoUrl;
    private String token;
    private String uid;
    private ArrayList<String> friends;
    private String lastLat;
    private String lastLng;

    public boolean isWantYouFriend = false;
    public boolean isWantMeFriend = false;

    public FCMProtocol.DataType mDataType = FCMProtocol.DataType.FRIEND_DELETE;

    public User()
    {

    }

    public User(String email, String displayName, String phoneNumber, String providerId, String photoUrl, String token, String uid)
    {
        setEmail(email);

        if( displayName == null || displayName.equalsIgnoreCase("null") )
            setDisplayName(email);
        else
            setDisplayName(displayName);

        setPhoneNumber(phoneNumber);
        setProviderId(providerId);
        setPhotoUrl(photoUrl);
        setToken(token);
        setUid(uid);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public void addFriend(String uid)
    {
        if( this.friends == null )
            this.friends = new ArrayList<String>();

        this.friends.add(uid);
    }

    public void removeFriend(String uid)
    {
        if( this.friends != null && this.friends.size() > 0 )
        {
            for( String user : this.friends )
            {
                if( user.equalsIgnoreCase(uid) ) {
                    this.friends.remove(user);
                    break;
                }
            }
        }
    }

    public String getLastLat() {
        return lastLat;
    }

    public void setLastLat(String lastLat) {
        this.lastLat = lastLat;
    }

    public String getLastLng() {
        return lastLng;
    }

    public void setLastLng(String lastLng) {
        this.lastLng = lastLng;
    }

    // 37.400847, 126.976332 인덕원
    // 37.399313, 126.969625 오비즈
}
