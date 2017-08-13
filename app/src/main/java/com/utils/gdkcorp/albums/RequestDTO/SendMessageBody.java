package com.utils.gdkcorp.albums.RequestDTO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class SendMessageBody {
    @SerializedName("to")
    private String to;
    @SerializedName("data")
    private DataBody data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public DataBody getData() {
        return data;
    }

    public void setData(DataBody data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SendMessageBody{" +
                "to='" + to + '\'' +
                ", data=" + data.toString() +
                '}';
    }
}
