package com.utils.gdkcorp.albums.ResponseDTO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class SendMessageResponse {
    @SerializedName("message_id")
    private String message_id;
    @SerializedName("error")
    private String error;

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "SendMessageResponse{" +
                "message_id='" + message_id + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
