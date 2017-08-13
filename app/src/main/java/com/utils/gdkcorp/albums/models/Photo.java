package com.utils.gdkcorp.albums.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Gautam Kakadiya on 10-08-2017.
 */

public class Photo {
    @SerializedName("picture_id")
    private String picture_id;
    @SerializedName("picture_url")
    private String picture_url;
    @SerializedName("taken_by")
    private String taken_by;
    @SerializedName("original_uri")
    private String original_uri;

    public String getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(String picture_id) {
        this.picture_id = picture_id;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getTaken_by() {
        return taken_by;
    }

    public void setTaken_by(String taken_by) {
        this.taken_by = taken_by;
    }

    public String getOriginal_uri() {
        return original_uri;
    }

    public void setOriginal_uri(String original_uri) {
        this.original_uri = original_uri;
    }
}
