package com.utils.gdkcorp.albums.models;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.pchmn.materialchips.model.ChipInterface;

import java.util.List;

/**
 * Created by Gautam Kakadiya on 10-08-2017.
 */

public class User implements ChipInterface,Parcelable {
    private String user_id;
    private String name_lowercase;
    private String name;
    private String profile_pic_url;
    private String registration_token;
    private BitmapDrawable profile_pic_drawable;

    public User(){

    }


    protected User(Parcel in) {
        user_id = in.readString();
        name_lowercase = in.readString();
        name = in.readString();
        profile_pic_url = in.readString();
        registration_token = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getRegistration_token() {
        return registration_token;
    }

    public void setRegistration_token(String registration_token) {
        this.registration_token = registration_token;
    }

    public void setProfile_pic_drawable(BitmapDrawable profile_pic_drawable) {
        this.profile_pic_drawable = profile_pic_drawable;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName_lowercase() {
        return name_lowercase;
    }

    public void setName_lowercase(String name_lowercase) {
        this.name_lowercase = name_lowercase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_pic_url() {
        return profile_pic_url;
    }

    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }

    @Override
    public Object getId() {
        return user_id;
    }

    @Override
    public Uri getAvatarUri() {
        return Uri.parse(profile_pic_url);
    }

    @Override
    public Drawable getAvatarDrawable() {
        return profile_pic_drawable;
    }

    @Override
    public String getLabel() {
        String[] names = name.split(" ");
        return names[0];
    }

    @Override
    public String getInfo() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(name_lowercase);
        parcel.writeString(name);
        parcel.writeString(profile_pic_url);
        parcel.writeString(registration_token);
    }
}
