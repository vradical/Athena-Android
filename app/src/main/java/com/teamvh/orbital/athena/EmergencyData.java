package com.teamvh.orbital.athena;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ron on 13-Jul-15.
 */
public class EmergencyData {

    String startTime;
    String endTime;
    String numOfTrack;
    String emID;
    String address;
    String country;
    String status;
    LatLng latlng;


    public String getEmID() {
        return emID;
    }

    public void setEmID(String emID) {
        this.emID = emID;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNumOfTrack() {
        return numOfTrack;
    }

    public void setNumOfTrack(String numOfTrack) {
        this.numOfTrack = numOfTrack;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }
}
