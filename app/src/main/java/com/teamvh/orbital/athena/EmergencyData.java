package com.teamvh.orbital.athena;

/**
 * Created by Ron on 13-Jul-15.
 */
public class EmergencyData {

    String startTime;
    String endTime;
    String numOfTrack;
    String emID;

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

}
