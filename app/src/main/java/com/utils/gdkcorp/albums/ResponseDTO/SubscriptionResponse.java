package com.utils.gdkcorp.albums.ResponseDTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class SubscriptionResponse {
    @SerializedName("results")
    private List<Object> results;

    public List<Object> getResults() {
        return results;
    }

    public void setResults(List<Object> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "SubscriptionResponse{" +
                "results=" + results +
                '}';
    }
}
