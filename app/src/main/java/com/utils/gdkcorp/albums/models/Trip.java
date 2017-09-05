package com.utils.gdkcorp.albums.models;

import java.util.List;

/**
 * Created by Gautam Kakadiya on 10-08-2017.
 */

public class Trip {
    private String id;
    private Boolean is_running;
    private String name;
    private String location;
    private String creator_id;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIs_running() {
        return is_running;
    }

    public void setIs_running(Boolean is_running) {
        this.is_running = is_running;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }
}
