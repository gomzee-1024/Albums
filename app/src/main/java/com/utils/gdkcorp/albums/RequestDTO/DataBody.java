package com.utils.gdkcorp.albums.RequestDTO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Gautam Kakadiya on 13-08-2017.
 */

public class DataBody {
    @SerializedName("trip_id")
    private String trip_id;
    @SerializedName("creator_id")
    private String creator_id;

    public DataBody(){

    }

    public DataBody(String tripId,String creatorId){
        this.trip_id = tripId;
        this.creator_id = creatorId;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    @Override
    public String toString() {
        return "DataBody{" +
                "trip_id='" + trip_id + '\'' +
                ", creator_id='" + creator_id + '\'' +
                '}';
    }
}
