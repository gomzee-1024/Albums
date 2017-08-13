package com.utils.gdkcorp.albums.RequestDTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class SubscriptionBody {
    @SerializedName("to")
    private String to;
    @SerializedName("registration_tokens")
    private List<String> tokens;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
}
